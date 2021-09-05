package me.clip.voteparty.conf.objects

internal data class SQLData(var user: String = "username",
                            var password: String = "password",
                            var database: String = "database",
                            var host: String = "host")
