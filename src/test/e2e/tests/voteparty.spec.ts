import { readFileSync, existsSync, readdirSync } from 'node:fs';
import { resolve } from 'node:path';
import assert from 'node:assert/strict';
import { test, expect, waitUntil, sleep } from '@plugwright/runner';

/**
 * VoteParty end-to-end suite: a real Paper server, a real shaded plugin jar, real bots.
 *
 * Two ways of driving the plugin, used for different jobs:
 *
 *   - `server.execute(cmd)` returns the command's own output, so admin commands are asserted
 *     against exact text. It goes through the console, which is always op.
 *   - `player.chat(cmd)` + `expect(player)` exercises the path a real player takes, and is how
 *     inventory effects are checked. A bot has to be op'd first; each test gets its own bot, so
 *     each one op's its own.
 *
 * Chat replies are MiniMessage/legacy coloured, so assertions leave room for the section sign
 * that sits between words.
 */

/**
 * The runner attaches to the server process after it has already printed its startup banner, so
 * `expect(server)` cannot see the plugin loading. The log file can. Where it lives comes from the
 * runner config the CLI was handed: `env.id` is the mode ("local"), which every environment
 * shares, so it cannot be used to tell the servers apart.
 */
function serverDir(): string {
    const configFlag = process.argv.indexOf('--config');
    if (configFlag === -1) {
        throw new Error('runner was started without --config');
    }
    return JSON.parse(readFileSync(process.argv[configFlag + 1], 'utf8')).environment.config.serverDir;
}

function logPath(): string {
    return resolve(serverDir(), 'logs/latest.log');
}

function serverLog(): string {
    const path = logPath();
    if (!existsSync(path)) {
        throw new Error(`server log not found at ${path}`);
    }
    return readFileSync(path, 'utf8');
}

/**
 * A marker for "everything the server logs from here on".
 *
 * The log file spans the whole session, so an assertion that a sentinel did *not* appear has to
 * look at what was appended after the marker, not at the whole file — otherwise it sees the copy
 * an earlier test produced.
 */
function logMark(): number {
    return serverLog().length;
}

function logSince(mark: number): string {
    return serverLog().slice(mark);
}

/** Strips the legacy colour codes a message is interleaved with, so text can be matched on. */
function plain(text: string): string {
    return text.replace(/\u00a7[0-9a-fk-or]/gi, '');
}

interface Console {
    execute(cmd: string): Promise<string>;
}

/**
 * The party vote counter, as the server currently has it.
 *
 * This is server-wide state that outlives any single test. It has to be read rather than assumed,
 * because a test that votes without reaching the threshold leaves a non-zero count behind for
 * whoever runs next.
 */
async function partyCounter(server: Console, player: string): Promise<number> {
    const raw = plain(await server.execute(`papi parse ${player} %voteparty_votes_recorded%`));
    const value = Number(raw.trim());

    assert.ok(Number.isFinite(value), `could not read the party counter, got ${JSON.stringify(raw)}`);
    return value;
}

/**
 * Sets how many further votes should trigger a party, leaving the count itself alone.
 *
 * `vp setcounter` only moves the threshold the count is measured against, so reading the count
 * and moving the threshold relative to it is enough to make a party's timing deterministic.
 *
 * Reaching the threshold to drive the count back to zero would also work, but it fires a real
 * party to do it — and that party's particles and rewards then land in whichever test runs next,
 * which is exactly the kind of leak this avoids.
 */
async function partyAfter(server: Console, player: string, votes: number): Promise<void> {
    await server.execute(`vp setcounter ${(await partyCounter(server, player)) + votes}`);
}

/**
 * Everything a test needs to be able to rely on, established rather than inherited: an op'd
 * player whose VoteParty vote count is zero, and a party that triggers after [partyAfterVotes]
 * more votes.
 */
/** EssentialsX's balance for a player, read through its own `/bal`, in whole dollars. */
async function balance(server: Console, player: string): Promise<number> {
    const raw = plain(await server.execute(`bal ${player}`));
    const match = /\$(\d+(?:\.\d+)?)/.exec(raw);
    assert.ok(match, `could not read a balance, got ${JSON.stringify(raw)}`);
    return Number(match[1]);
}

/** Grants a permission through LuckPerms, which is what EssentialsX and Vault now defer to. */
async function grant(server: Console, player: string, permission: string): Promise<void> {
    await server.execute(`lp user ${player} permission set ${permission} true`);
    await server.execute('lp save');
    // LuckPerms is asynchronous, and EssentialsX caches permissions, so give the recalculation a
    // moment before asserting on what a gated reward did or did not do.
    await sleep(2000);
}


async function opAndPrep(server: Console, username: string, partyAfterVotes = 5): Promise<void> {
    await server.execute(`op ${username}`);
    // VoteParty only knows a player once they have joined, so the bot has to be online before
    // any vote command will accept its name.
    await server.execute(`vp resetvotes ${username}`);
    await partyAfter(server, username, partyAfterVotes);
}

test('the plugin enables and registers its PlaceholderAPI expansion', async () => {
    const log = serverLog();

    assert.match(log, /Enabling VoteParty v/, 'VoteParty did not enable');
    assert.match(log, /Successfully registered internal expansion: voteparty \[2\.0\]/);
    assert.doesNotMatch(log, /Could not load 'plugins\/VoteParty/);
});

/**
 * `%player_name%` is not built into PlaceholderAPI; it comes from the separately distributed
 * Player expansion, and every user-scoped reward command in the shipped config uses it. Without
 * this expansion loaded, `formMessage()` hands those commands to the server with the placeholder
 * still in them and the command dispatch throws.
 *
 * This runs first: the download is asynchronous and needs a reload to take effect.
 */
test('the Player placeholder expansion can be downloaded and loaded', async ({ player, server }) => {
    await server.execute(`op ${player.username}`);
    const expansions = resolve(serverDir(), 'plugins/PlaceholderAPI/expansions');

    await server.execute('papi ecloud download Player');
    // The download runs on PlaceholderAPI's own thread and says so over RCON rather than on the
    // console, so the only way to see it land is the jar appearing. Reloading while a download
    // is still in flight cancels it outright.
    await waitUntil(
        () => existsSync(expansions) && readdirSync(expansions).some((f) => f.toLowerCase().includes('player')),
        { timeout: 30000, interval: 500, message: 'the Player expansion was never downloaded' },
    );
    await server.execute('papi reload');

    await waitUntil(
        async () => plain(await server.execute(`papi parse ${player.username} %player_name%`)).trim() === player.username,
        { timeout: 30000, interval: 1000, message: '%player_name% never started resolving' },
    );

    // Reloading PlaceholderAPI must not cost VoteParty its internal expansion.
    assert.match(plain(await server.execute(`papi parse ${player.username} %voteparty_votes_recorded%`)), /\d+/);
});

test('a player can run the help command', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    player.chat('/vp help');
    await expect(player).toHaveReceivedMessage(/VoteParty Help/);
    await expect(player).toHaveReceivedMessage(/\/vp addvote/);
});

test('votes are recorded, counted and resettable', async ({ player, server }) => {
    const target = player.username;
    await opAndPrep(server, target);

    assert.match(plain(await server.execute(`vp addvote ${target} true 5`)), /You've given 5 votes/);

    assert.match(plain(await server.execute(`vp totalvotes ${target}`)), /has a total of 5 vote\(s\)/);
    assert.match(
        plain(await server.execute(`vp checkvotes ${target} 1 days`)),
        /has voted 5 times in the last 1 days/,
    );

    assert.match(plain(await server.execute(`vp resetvotes ${target}`)), /vote count has been reset to 0/);
    assert.match(plain(await server.execute(`vp totalvotes ${target}`)), /has a total of 0 vote\(s\)/);
});

test('a vote pays the per-vote reward and announces the vote', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    // Not silent: per-vote rewards hang off VoteReceivedEvent, which a silent vote never fires.
    assert.match(plain(await server.execute(`vp addvote ${player.username} false 1`)), /You've given 1 votes/);

    // Both shipped rewards are `give %player_name% <item>` commands, so the items only land if
    // the placeholder was expanded for the voter. EssentialsX owns /give here and resolves the
    // legacy `STEAK` to beef.
    await expect(player).toContainItem('beef');
    await expect(player).toContainItem('golden_apple');
    await expect(server).toHaveReceivedMessage(new RegExp(`${player.username} just voted!`));
});

test('an unknown player is rejected rather than crashing', async ({ server }) => {
    assert.match(
        plain(await server.execute('vp addvote DefinitelyNotAPlayer true 1')),
        /could not be found/,
    );
});

/**
 * The shipped effects are enabled in the fixture and name SMOKE and HEART, neither of which the
 * removed EffectType enum had a constant for. Asserting the particles actually arrive is what
 * proves the names resolved to real particles on this server, rather than merely not throwing:
 * a silently skipped particle would pass a "no exception" check too.
 *
 * The vote group also names NOT_A_REAL_PARTICLE, which no version has. It must be skipped
 * without taking the rest of the vote down with it.
 */
test('the configured particles spawn on a vote, and an unknown name is skipped', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    // mineflayer's Particle type does not declare `name`, although the runtime object carries it
    // from the particle registry, so the handler takes the value untyped and narrows itself.
    const seen = new Set<string>();
    const listener = (particle: unknown) => {
        const name = (particle as { name?: string }).name;
        if (name) seen.add(name.toLowerCase());
    };
    player.bot.on('particle', listener);

    try {
        assert.match(plain(await server.execute(`vp addvote ${player.username} false 1`)), /You've given 1 votes/);

        await waitUntil(() => [...seen].some((name) => name.includes('smoke')) && [...seen].some((name) => name.includes('heart')), {
            timeout: 15000,
            interval: 250,
            message: `expected smoke and heart particles, saw: ${[...seen].join(', ') || 'nothing'}`,
        });
    } finally {
        player.bot.removeListener('particle', listener);
    }

    // Reaching here with the unknown name still in the list means nothing above threw; the log
    // assertion at the end of the suite is what actually checks that.
    assert.ok(seen.size > 0);
});

test('the party vote counter moves in both directions', async ({ player, server }) => {
    // The threshold goes well out of reach so these additions accumulate rather than resetting
    // themselves.
    await partyAfter(server, player.username, 100);

    // Put the counter somewhere deliberately non-zero. Everything below is stated against the
    // count read back, which is the whole point: it has to hold whatever the count was, so the
    // test is meaningful whether or not an earlier test left something behind.
    await server.execute('vp addpartyvote 4');
    const start = await partyCounter(server, player.username);
    assert.ok(start >= 4, `expected a non-zero counter to work from, got ${start}`);

    assert.match(plain(await server.execute('vp addpartyvote 1')), new RegExp(`Current votes updated to ${start + 1}\\b`));
    assert.match(plain(await server.execute('vp addpartyvote 2')), new RegExp(`Current votes updated to ${start + 3}\\b`));
    assert.match(plain(await server.execute('vp setcounter 5')), /New required votes has been set/);
    assert.match(plain(await server.execute('vp setcounter -1')), /must be positive/);
});

test('a party runs its pre, main and post commands in order', async ({ server }) => {
    // startparty is unconditional, so this needs no counter state at all.
    assert.match(await server.execute('vp startparty'), /force started a .*Vote Party/);

    await expect(server).toHaveReceivedMessage(/PRE_PARTY_COMMAND/);
    await expect(server).toHaveReceivedMessage(/PARTY_COMMAND/);
    await expect(server).toHaveReceivedMessage(/POST_PARTY_COMMAND/);
});

test('reaching the vote threshold by voting triggers a party', async ({ player, server }) => {
    await opAndPrep(server, player.username, 5);

    // Not silent: this fires VoteReceivedEvent, which is what moves the party counter.
    assert.match(plain(await server.execute(`vp addvote ${player.username} false 5`)), /You've given 5 votes/);

    await expect(server).toHaveReceivedMessage(/PRE_PARTY_COMMAND/);
    await expect(server).toHaveReceivedMessage(/POST_PARTY_COMMAND/);
});

test('a party started from votes pays every online player', async ({ player, server }) => {
    await opAndPrep(server, player.username, 5);
    await server.execute(`vp addvote ${player.username} false 5`);

    await expect(player).toContainItem('golden_apple');
});

test('a private party pays the player it was given to', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    player.chat(`/vp giveparty ${player.username}`);
    await expect(player).toHaveReceivedMessage(/You've received a private .*Vote Party/);
    await expect(player).toContainItem('golden_apple');
});

test('the PlaceholderAPI expansion resolves', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    // An absolute threshold, so the expectation does not depend on whatever the counter holds.
    await server.execute('vp setcounter 7');

    const required = plain(await server.execute(`papi parse ${player.username} %voteparty_votes_required_total%`));
    assert.equal(required.trim(), '7');

    // votes_recorded is the raw party counter, read back through the expansion.
    assert.equal(
        plain(await server.execute(`papi parse ${player.username} %voteparty_votes_recorded%`)).trim(),
        String(await partyCounter(server, player.username)),
    );
});

test('the leaderboard command responds', async ({ player, server }) => {
    await opAndPrep(server, player.username);
    await server.execute(`vp addvote ${player.username} false 1`);

    // Leaderboards are snapshotted when the plugin loads and only refreshed two minutes after
    // that, and /vp reload does not rebuild them, so a freshly started server has no entries.
    // The command has to answer with something sane rather than the header or nothing.
    assert.match(plain(await server.execute('vp top alltime 1')), /Top Voters|page number is invalid/);
});

test('the config reloads cleanly', async ({ player, server }) => {
    await opAndPrep(server, player.username);

    player.chat('/vp reload');
    await expect(player).toHaveReceivedMessage(/The config has been reloaded/);
});
/**
 * The shipped party reward is `eco give %player_name% 100`, which needs Vault and an economy
 * provider. Both are installed for this suite, so the assertion is on the balance moving by an
 * exact amount rather than on the command being dispatched.
 */
test('a party pays an economy reward through Vault', async ({ player, server }) => {
    await opAndPrep(server, player.username, 5);
    await server.execute(`eco give ${player.username} 0`);

    const before = await balance(server, player.username);
    await server.execute(`vp addvote ${player.username} false 5`);

    await waitUntil(async () => (await balance(server, player.username)) === before + 100, {
        timeout: 15000,
        interval: 500,
        message: `expected the balance to go from ${before} to ${before + 100}`,
    });
});

/**
 * One player, one test, differing only in what LuckPerms says it holds.
 *
 * opAndPrep makes the bot op, and an op holds every permission by default, so a granted
 * assertion on top of that proves nothing — it would pass with LuckPerms doing nothing at all.
 * The bot is de-op'd first, which establishes a genuine denied baseline, and the votes come from
 * the console so the de-op does not stop the vote from being cast.
 */
test('a permission-gated vote reward follows the permission LuckPerms grants', async ({ player, server }) => {
    const target = player.username;
    await opAndPrep(server, target);
    await server.execute(`deop ${target}`);

    // Baseline: no permission held, so the gated reward must not run.
    let mark = logMark();
    await server.execute(`vp addvote ${target} false 1`);
    await sleep(3000);

    // The vote itself has to have gone through, or the absence proves nothing.
    assert.match(plain(await server.execute(`vp totalvotes ${target}`)), /has a total of 1 vote/);
    assert.match(logSince(mark), /just voted!/);
    assert.doesNotMatch(logSince(mark), /PERMISSION_VOTE_REWARD/);

    // Now the only difference is the permission, granted through LuckPerms.
    await grant(server, target, 'my.special.permission');
    await server.execute(`vp resetvotes ${target}`);

    mark = logMark();
    await server.execute(`vp addvote ${target} false 1`);

    await waitUntil(() => /PERMISSION_VOTE_REWARD/.test(logSince(mark)), {
        timeout: 10000,
        interval: 250,
        message: 'the permission-gated vote reward never ran once LuckPerms granted the permission',
    });
});

/** The same denied-baseline-then-grant shape as the vote reward above, on the party path. */
test('a permission-gated party reward follows the permission LuckPerms grants', async ({ player, server }) => {
    const target = player.username;
    await opAndPrep(server, target, 5);
    await server.execute(`deop ${target}`);

    let mark = logMark();
    await server.execute('vp startparty');
    await waitUntil(() => /POST_PARTY_COMMAND/.test(logSince(mark)), {
        timeout: 15000,
        interval: 250,
        message: 'the party itself never ran, so the absence would prove nothing',
    });
    assert.doesNotMatch(logSince(mark), /PERMISSION_PARTY_REWARD/);

    await grant(server, target, 'my.special.permission');

    mark = logMark();
    await server.execute('vp startparty');

    await waitUntil(() => /PERMISSION_PARTY_REWARD/.test(logSince(mark)), {
        timeout: 15000,
        interval: 250,
        message: 'the permission-gated party reward never ran once LuckPerms granted the permission',
    });
});

/**
 * NuVotifier is a soft dependency, so without it VoteParty logs a listener-registration error on
 * every startup and its Votifier hook is dead code. With it installed, `/testvote` drives a real
 * vote through NuVotifier into VoteParty's own listener.
 */
test('a vote arriving through NuVotifier is recorded', async ({ player, server }) => {
    await opAndPrep(server, player.username, 50);

    player.chat(`/testvote ${player.username}`);

    await waitUntil(
        async () => /has a total of 1 vote/.test(plain(await server.execute(`vp totalvotes ${player.username}`))),
        { timeout: 15000, interval: 500, message: 'the NuVotifier vote never reached VoteParty' },
    );
});

test('the NuVotifier hook registers its listener', async () => {
    const log = serverLog();

    assert.doesNotMatch(log, /Failed to register events for class .*HooksListenerNuVotifier/);
    assert.match(log, /\[Votifier\] Enabling Votifier/);
});

test('no plugin exception was logged for the whole session, including the plugin-backed specs', async () => {
    const log = serverLog();

    // The NuVotifier listener error is expected on a server without Votifier: VoteParty
    // soft-depends on it, and the listener class cannot resolve its event type.
    assert.doesNotMatch(log, /Exception in server tick loop/);
    assert.doesNotMatch(log, /Could not pass event .* to VoteParty/);
    assert.doesNotMatch(log, /\[VoteParty\]\[ACF\] Exception in command/);
    assert.doesNotMatch(log, /\[VoteParty\].*Task #\d+ for VoteParty .* generated an exception/);
    assert.doesNotMatch(log, /java\.lang\.(NoSuchMethod|NoClassDef|NoField)Error/);
});
