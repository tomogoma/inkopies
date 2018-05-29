package ke.co.definition.inkopies.presentation.format

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by tomogoma
 * On 29/05/18.
 */
interface DateFormatter {
    fun formatDate(date: Date): String
}

class DateFormatterImpl : DateFormatter {
    override fun formatDate(date: Date): String {
        return SimpleDateFormat.getDateInstance().format(date)
    }
}