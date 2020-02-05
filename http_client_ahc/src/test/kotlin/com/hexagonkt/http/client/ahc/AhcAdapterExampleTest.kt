package com.hexagonkt.http.client.ahc

import com.hexagonkt.http.client.ClientTest
import org.testng.annotations.Test

val adapter = AhcAdapter()

@Test class AhcAdapterExampleTest : ClientTest(adapter)
