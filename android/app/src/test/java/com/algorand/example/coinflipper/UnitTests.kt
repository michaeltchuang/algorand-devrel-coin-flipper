package com.algorand.example.coinflipper

import com.algorand.example.coinflipper.data.AlgorandRepository
import com.algorand.example.coinflipper.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTests {
    val respository = AlgorandRepository()

    @Test
    fun restoreAccount() {
        val result =
            respository.recoverAccount(
                Constants.TEST_PASSPHRASE_PART1 +
                    " " + Constants.TEST_PASSPHRASE_PART2,
            )
        assertEquals(Constants.TEST_ADDRESS, result?.address?.toString())
    }
}
