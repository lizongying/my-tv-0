import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lizongying.mytv0.models.TVList
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<String>()
    val data: LiveData<String> get() = _data

    fun updateData(newData: String) {
        _data.value = newData
    }

    fun updateEPG() {
        viewModelScope.launch {
            TVList.updateEPG()
        }
    }
}