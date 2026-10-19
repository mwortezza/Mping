package com.example

import com.example.engine.XrayConfigGenerator
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import com.example.model.TunnelConfig
import com.example.parser.MpingUriParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MpingCoreUnitTest {

    @Test
    fun testParseVlessRealityUri() {
        val testUri = "vless://a3b98d41-7681-4328-98e2-82dc35b67812@104.16.132.229:443?type=tcp&security=reality&sni=gateway.icloud.com&flow=xtls-rprx-vision&pbk=2x37aL8mJ79aN82vQ1Z5rV9xLmO3pQ4a&sid=6ba85301#US-East-Node"
        val profile = MpingUriParser.parse(testUri)

        assertNotNull(profile)
        assertEquals(ProxyProtocol.VLESS, profile?.protocol)
        assertEquals("104.16.132.229", profile?.serverAddress)
        assertEquals(443, profile?.serverPort)
        assertEquals("reality", profile?.security)
        assertEquals("gateway.icloud.com", profile?.sni)
        assertEquals("xtls-rprx-vision", profile?.flow)
        assertEquals("US-East-Node", profile?.name)
    }

    @Test
    fun testParseTrojanUri() {
        val testUri = "trojan://password123@1.1.1.1:443?security=tls&sni=example.com#Frankfurt-Trojan"
        val profile = MpingUriParser.parse(testUri)

        assertNotNull(profile)
        assertEquals(ProxyProtocol.TROJAN, profile?.protocol)
        assertEquals("1.1.1.1", profile?.serverAddress)
        assertEquals(443, profile?.serverPort)
        assertEquals("password123", profile?.uuidOrPassword)
        assertEquals("Frankfurt-Trojan", profile?.name)
    }

    @Test
    fun testXrayJsonGeneration() {
        val profile = ServerProfile(
            id = "test-01",
            name = "Test VLESS Node",
            protocol = ProxyProtocol.VLESS,
            serverAddress = "192.168.1.100",
            serverPort = 8443,
            uuidOrPassword = "test-uuid-val",
            security = "reality",
            sni = "test.domain.com",
            publicKey = "pbk-test",
            shortId = "sid-test"
        )
        val config = TunnelConfig()
        val jsonStr = XrayConfigGenerator.generateXrayJson(profile, config)

        val root = JSONObject(jsonStr)
        assertTrue(root.has("inbounds"))
        assertTrue(root.has("outbounds"))
        assertTrue(root.has("routing"))
        assertTrue(root.has("dns"))

        val inbounds = root.getJSONArray("inbounds")
        assertEquals(2, inbounds.length())
        assertEquals(10808, inbounds.getJSONObject(0).getInt("port"))
    }
}
