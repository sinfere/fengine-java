package com.dix.fengine.app.client

enum class TransportProtocol(val value: Int) {
    TCP(1),
    UDP(2),
    WEBSOCKET(3),
    MQ(4),
}

class Session(
        val key: String,
        val transportProtocol: TransportProtocol,
        val protocolId: Int,
        val host: String,
        val port: Int,
        val space: String,
        val clientId: String,
        val serverId: String,
        val createTime: Long,
        var updateTime: Long,
        var lastActiveTime: Long
) {
}