package dev.cardoso.quotesmvvm.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dev.cardoso.quotesmvvm.R
import dev.cardoso.quotesmvvm.data.model.QuoteModel
import dev.cardoso.quotesmvvm.databinding.FragmentHomeBinding
import dev.cardoso.quotesmvvm.domain.UserPreferencesRepository
import dev.cardoso.quotesmvvm.domain.usecase.DeleteQuoteUseCase
import dev.cardoso.quotesmvvm.presentation.view.EditQuoteFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment()  {
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private var lastId= 0
    private lateinit var quoteList: List<QuoteModel>
    private var token: String =""


    private val homeViewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding2: FragmentHomeBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesRepository = UserPreferencesRepository(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getToken()

        Log.w("debug", "el token es $token")
        binding2 = FragmentHomeBinding.inflate(layoutInflater)


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnAgregarFrase.setOnClickListener {
            doSomething()
           // testViewModel("Bearer S")
        }
        getQuotes("Bearer S")
       return root

       // return binding2.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun doSomething (){
        Log.w("debug", "somehting")
        lastId= quoteList.elementAt(quoteList.lastIndex).id+1
        val bundle = bundleOf("lastId" to lastId,)
        this.findNavController().navigate(R.id.action_nav_home_to_addQuoteFragment, bundle)

    }


    private fun getQuotes(token:String){
        viewLifecycleOwner.lifecycleScope.launch{
            Log.w("debug", "el token es ... $token")

            homeViewModel.getQuotes("Bearer $token")
            homeViewModel.quoteList.collect{
                Log.w("debug", "$it")
                binding.rvFrases.adapter= QuotesAdapter(it,
                    object:QuotesAdapter.OptionsClickListener{
                        override fun onUpdateQuote(quote: QuoteModel) {
                            adapterOnClick(quote)
                        }

                        override fun onDeleteQuote(quote: QuoteModel) {
                            lifecycleScope.launch (Dispatchers.IO){
                                userPreferencesRepository.token.collect {
                                    deleteQuote("Bearer $it", id= quote.id)
                                }
                            }

                        }

                        override fun onMenuClicked(context: Context, position: Int, quote: QuoteModel) {
                            performOptionsMenuClick(context, position, quote)
                        }
                    }
                )
                quoteList=it

            }
        }

    }
    private fun testViewModel(token:String){
        this.lifecycleScope.launch{
            Log.w("debug", "el token es ... $token")

           homeViewModel.getQuotes("Bearer $token")
        }
    }

    private fun adapterOnClick (quote: QuoteModel) {
         //Toast.makeText(context, "El id es : ${quote.id}", Toast.LENGTH_LONG).show()
        val action = HomeFragmentDirections.actionNavHomeToEditQuoteFragment(
            quote.quote,
            quote.author
        )
        action.quoteId= quote.id
        this.findNavController().navigate(action)
    }

    private fun deleteQuote(token:String, id:Int){
        homeViewModel.deleteQuote(token, id)
    }


    private fun performOptionsMenuClick(context: Context, position:Int, quote: QuoteModel) {
        // create object of PopupMenu and pass context and view where we want
        // to show the popup menu
        val popupMenu = PopupMenu(context,
            _binding?.rvFrases?.get(position)?.findViewById(R.id.textViewOptions)
        )
        // add the menu
        popupMenu.inflate(R.menu.options_menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.deleteQuoteOption -> {
                        Toast.makeText(context, "Deleting quote", Toast.LENGTH_SHORT)
                            .show()
                        deleteQuote("Bearer $token", id= quote.id)
                        getQuotes(token)
                        binding.rvFrases.adapter?.notifyDataSetChanged()
                    }
                    // in the same way you can implement others
                    R.id.editQuoteOption -> {
                        // define

                        Toast.makeText(context, "Edit quote", Toast.LENGTH_SHORT)
                            .show()
                        adapterOnClick(quote)
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    private fun getMockQuotes(token:String){
        viewLifecycleOwner.lifecycleScope.launch{
            Log.w("debug", "el token es ... $token")

            homeViewModel.getQuotes("Bearer $token")

            val it= listOf(QuoteModel(1,"1", "1"), QuoteModel(2,"2", "" +
                    "2z"),QuoteModel(3,"3", "3"), QuoteModel(4,"4", "4"))
            binding.rvFrases.adapter= QuotesAdapter(it
                ,
                object:QuotesAdapter.OptionsClickListener{
                    override fun onUpdateQuote(quote: QuoteModel) {
                        adapterOnClick(quote)
                    }

                    override fun onDeleteQuote(quote: QuoteModel) {
                        deleteQuote("Bearer $token", id= quote.id)
                        getQuotes(token)
                        binding.rvFrases.adapter?.notifyDataSetChanged()

                    }

                    override fun onMenuClicked(context: Context, position: Int, quote: QuoteModel) {
                        performOptionsMenuClick(context, position, quote)
                    }
                }
            )
            quoteList=it

        }

    }

    override fun onResume() {
        super.onResume()
        getQuotes(token)
        binding.rvFrases.adapter?.notifyDataSetChanged()

    }

    private fun getToken(){
        lifecycleScope.launch (Dispatchers.IO){
            userPreferencesRepository.token.collect {
                token = it
                Log.w("debug", " fun getoken home::: $it")
            }
        }
    }
}