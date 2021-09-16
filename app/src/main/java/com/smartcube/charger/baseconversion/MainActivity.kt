package com.smartcube.charger.baseconversion

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.smartcube.charger.baseconversion.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityMainBinding
    private val activity = this
    private val context: Context = this
    var inputBase = 10
    var outputBase = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(activity, R.layout.activity_main)

        mBinding.edtInputBase.addTextChangedListener {
            try {
                if (it.toString().isNotBlank()) {
                    inputBase = it.toString().toInt()
                    if (mBinding.edtInput.toString().isNotBlank())
                        mBinding.tvOutput.text = mBinding.edtInput.text.toString().transferIntAndDec(inputBase, outputBase)
                } else
                    mBinding.tvOutput.text = ""
            } catch (e: TransFerException) {
                mBinding.tvOutput.text = e.className
            }
        }
        mBinding.edtOutputBas.addTextChangedListener {
            try {
                if (it.toString().isNotBlank()) {
                    outputBase = it.toString().toInt()
                    if (mBinding.edtInput.toString().isNotBlank())
                        mBinding.tvOutput.text = mBinding.edtInput.text.toString().transferIntAndDec(inputBase, outputBase)
                } else
                    mBinding.tvOutput.text = ""
            } catch (e: TransFerException) {
                mBinding.tvOutput.text = e.className
            }
        }
        mBinding.edtInput.addTextChangedListener {
            try {
                if (it.toString().isNotBlank())
                    mBinding.tvOutput.text =
                        it.toString().transferIntAndDec(inputBase, outputBase)
                else
                    mBinding.tvOutput.text = ""
            } catch (e: TransFerException) {
                mBinding.tvOutput.text = e.className
            }
        }
    }
}
@Throws(TransFerException::class)
private fun String.transferIntAndDec(inputBase: Int, outputBase: Int): String {
    return if(this.contains(".")){
        val intAndDecSub = this.split(".")
        if(intAndDecSub.size >2)
            throw TransFerException("輸入錯誤(超過一個小數點)")

        "${intAndDecSub[0].intToDecimal(inputBase).intToBase(outputBase)}.${intAndDecSub[1].decToDecimal(inputBase, intAndDecSub[1].length).decToBase(outputBase,intAndDecSub[1].length)}"

    }else
        this.intToDecimal(inputBase).intToBase(outputBase)
}

/**整數部分─
 * 依照輸入的base進制先轉換為十進位
 * 作法：次方相加
 * */
@Throws(TransFerException::class)
private fun String.intToDecimal(base: Int): Int {
    //轉換為十進位(次方相加)：
    println("====計算開始====")
    var result = 0
    var index = this.length - 1 //從最後一個位數開始取，取到第0個為止。
    var pow = 0 //次方數，由0開始加到this.length-1
    while (index > -1) {
        if (this[index].transferToInt() > base - 1)
            throw TransFerException("輸入錯誤(超過轉換位數)")
        result += this[index].transferToInt() * base.toDouble().pow(pow.toDouble()).toInt()
        pow++
        index--
    }
    return result
}

/**整數部分─
 * 10進制轉為base進制
 * 作法：短除法：依照如果大於base就要繼續除的原則，不斷除餘數，直到小於(不等於)base，所得到的餘數字串反轉為結果。*/
@Throws(TransFerException::class)
private fun Int.intToBase(base: Int): String {
    if (base <= 1)
        return ""
    var result = ""
    var dividend = this //被除數，會一直變小
    while (dividend != 0) {
        result += (dividend % base).transferToStr()
        dividend /= base
    }
    return result.reversed()
}

/**小數部分─
 * 依照輸入的base進制轉換為十進位
 * 作法：次方相加
 * */
@Throws(TransFerException::class)
private fun String.decToDecimal(base: Int, digit: Int): BigDecimal {
    println("====計算開始====,,,轉十進制前要計算之值是===>$this")
    val useDigit = 100
    var result = BigDecimal(0.0)
    //轉換為十進位(次方相加)：
    var index = 0 //從最後一個位數開始取，取到第0個為止。
    var pow = -1 //次方數，由-1開始加到-this.length
    while (index < this.length) {
        if (this[index].transferToInt() > base - 1)
            throw TransFerException("輸入錯誤(超過轉換位數)")
        println("現在的數字是===>${this[index].transferToInt()},,,底數是===>${base.toDouble()},,,次方數是===>$pow,,,次方結果是===>${BigDecimal(base).pow(pow, MathContext(useDigit))},,,此階段計算總合為===>${BigDecimal(this[index].transferToInt().toDouble()) * BigDecimal(base).pow(pow, MathContext(useDigit))}")
        result += BigDecimal(this[index].transferToInt().toDouble()) * BigDecimal(base).pow(pow, MathContext(useDigit))
        pow--
        index++
    }

    return result
}

/**小數部分─
 * 10進制轉為base進制
 * 作法：乘以base，取大於0的部分(直到小數部分全部清空為止)
 * */
@Throws(TransFerException::class)
private fun BigDecimal.decToBase(base: Int, digit: Int): String {
    println("====計算開始====,,,轉base進制前要計算之值是$this")
    val useDigit = 100
    var result = ""
    var multiplicand = this //被乘數，大於0的部分會不斷的減掉
    var count = 0
    while (multiplicand > BigDecimal(0.0) && count < 19) {
        multiplicand *= BigDecimal(base)
//        multiplicand= multiplicand.format(format)
        multiplicand = multiplicand.formatRoundDown(useDigit)
        result += multiplicand.toInt().transferToStr()
//        println("減之前的 multiplicand 是===>$multiplicand")
        multiplicand = (multiplicand - multiplicand.formatRoundDown(0)).formatRoundDown(useDigit)
//        println("減完的 multiplicand 是===>$multiplicand")
        count++
    }
    return result
}

fun BigDecimal.formatRoundDown(digit: Int): BigDecimal = this.setScale(digit, BigDecimal.ROUND_DOWN)

/**用於控制小數精度，傳入多少個digit，就要回傳幾個#*/
fun getFormat(digit: Int): String {
    var result = ""
    for (i in 0 until digit) {
        result += "#"
    }
//    println("回傳前，result是===>$result")
    return "#.$result"

}


private fun Double.format(format: String = "#.0") = DecimalFormat(format).format(this).toDouble()

private fun Char.transferToInt(): Int {
//    println("thisChar===>${this.toInt()}")
    return when (this) {
        48.toChar() -> 0
        49.toChar() -> 1
        50.toChar() -> 2
        51.toChar() -> 3
        52.toChar() -> 4
        53.toChar() -> 5
        54.toChar() -> 6
        55.toChar() -> 7
        56.toChar() -> 8
        57.toChar() -> 9
        65.toChar() -> 10//A
        66.toChar() -> 11
        67.toChar() -> 12
        68.toChar() -> 13
        69.toChar() -> 14
        70.toChar() -> 15
        71.toChar() -> 16

        97.toChar() -> 10//a
        98.toChar() -> 11
        99.toChar() -> 12
        100.toChar() -> 13
        101.toChar() -> 14
        102.toChar() -> 15
        103.toChar() -> 16

        else -> 0
    }

}

private fun Int.transferToStr(): String {
    return when (this) {
        0 -> "0"
        1 -> "1"
        2 -> "2"
        3 -> "3"
        4 -> "4"
        5 -> "5"
        6 -> "6"
        7 -> "7"
        8 -> "8"
        9 -> "9"
        10 -> "A"
        11 -> "B"
        12 -> "C"
        13 -> "D"
        14 -> "E"
        15 -> "F"
        16 -> "G"
        else -> ""
    }

}
