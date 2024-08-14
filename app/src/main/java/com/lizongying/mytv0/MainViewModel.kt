import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lizongying.mytv0.SP
import com.lizongying.mytv0.Utils.getDateFormat
import com.lizongying.mytv0.models.TVList
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    private var timeFormat = if (SP.displaySeconds) "HH:mm:ss" else "HH:mm"

    fun setDisplaySeconds(displaySeconds: Boolean) {
        timeFormat = if (displaySeconds) "HH:mm:ss" else "HH:mm"
        SP.displaySeconds = displaySeconds
    }

    fun getTime(): String {
        return getDateFormat(timeFormat)
    }

    fun updateEPG() {
        viewModelScope.launch {
            TVList.updateEPG()
        }
    }
}