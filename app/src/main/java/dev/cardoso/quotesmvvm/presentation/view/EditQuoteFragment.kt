package dev.cardoso.quotesmvvm.presentation.view

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.cardoso.quotesmvvm.R
import dev.cardoso.quotesmvvm.data.model.QuoteRequest
import dev.cardoso.quotesmvvm.databinding.FragmentEditQuoteBinding
import dev.cardoso.quotesmvvm.domain.UserPreferencesRepository
import dev.cardoso.quotesmvvm.presentation.viewmodel.EditQuoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditQuoteFragment : Fragment() {
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private var token: String =""

    private val editQuoteViewModel: EditQuoteViewModel by viewModels()

    private  var _binding: FragmentEditQuoteBinding?  = null
    private val binding get()= _binding!!

    val args: EditQuoteFragmentArgs by navArgs()
    var author=""
    var quotText=""
    var quoteId=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        author= args.quoteAuthor
        quotText = args.quoteText
        quoteId= args.quoteId

        userPreferencesRepository = UserPreferencesRepository(requireActivity())
        getToken()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditQuoteBinding.inflate(inflater, container, false)
        val root:View = binding.root
        setBtnCancelListener()

        setBtnCreateListener()
        setTextFieldsListener()
        setCurrentText()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // methods

    private fun getToken(){
        lifecycleScope.launch (Dispatchers.IO){
            userPreferencesRepository.token.collect {
                token = it
            }
        }
    }

    private fun setBtnCancelListener() {
        binding.btnCancelEdit.setOnClickListener{
            this.findNavController().popBackStack()
//            this.findNavController().navigate(R.id.action_EditQuoteFragment_to_nav_home)
        }
    }

    fun setTextFieldsListener(){
        binding.etEditAuthor.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        })
        binding.etEditQuote.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        })
        binding.etEditQuote.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.etEditAuthor.setRawInputType(InputType.TYPE_CLASS_TEXT);
        binding.etEditAuthor.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.etEditQuote.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }


    fun setBtnCreateListener() {
        binding.btnEditQuote.setOnClickListener {

            if (!validInput() ){
                Toast.makeText(requireContext(), "Author and Quote fields must not be empty", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(), "Editing Quote ...", Toast.LENGTH_SHORT).show()
                Log.w("debug", "${binding.etEditQuote.text.toString()}, ${binding.etEditAuthor.text.toString()}")

                editQuoteViewModel.editQuote("Bearer $token", QuoteRequest(binding.etEditQuote.text.toString(), binding.etEditAuthor.text.toString()), quoteId.toString())
                lifecycleScope.launch(){
                    editQuoteViewModel.quoteResponse.collect{
                        if(it.success){
                            Toast.makeText(requireContext(), "guardado correctamente", Toast.LENGTH_LONG).show()
                        }else{
                            if (it.message!=""){
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                this.findNavController().navigate(R.id.action_editQuoteFragment_to_nav_home)
            }
        }
    }
    private fun validInput():Boolean{
        return binding.etEditAuthor.text.toString().isNotEmpty() &&
                binding.etEditQuote.text.toString().isNotEmpty()
    }

    private fun setCurrentText(){
      binding.etEditAuthor.setText(author)
      binding.etEditQuote.setText(quotText)
    }
}