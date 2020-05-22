package com.hexagonkt.http.client.ahc

import com.hexagonkt.http.client.ClientTest
import org.testng.annotations.Test

@Test class AhcAdapterExampleTest : ClientTest({ AhcAdapter() })
