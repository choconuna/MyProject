package org.techtown.myproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    private val _searchData = MutableLiveData<SearchData>()
    val searchData: LiveData<SearchData>
        get() = _searchData

    fun setValues(category: String?, date: String?, startDate: String?, endDate: String?, searchText: String?) {
        val data = SearchData(category, date, startDate, endDate, searchText)
        _searchData.value = data
    }
}