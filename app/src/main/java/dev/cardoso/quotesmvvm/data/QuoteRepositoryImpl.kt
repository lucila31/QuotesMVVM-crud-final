package dev.cardoso.quotesmvvm.data

import android.accounts.NetworkErrorException
import android.util.Log
import dev.cardoso.quotesmvvm.core.convertToList
import dev.cardoso.quotesmvvm.data.local.QuoteLocalDataSource
import dev.cardoso.quotesmvvm.data.local.daos.QuoteDAO
import dev.cardoso.quotesmvvm.data.model.AddQuoteResponse
import dev.cardoso.quotesmvvm.data.model.QuoteModel
import dev.cardoso.quotesmvvm.data.model.QuoteRequest
import dev.cardoso.quotesmvvm.data.model.QuoteResponse
import dev.cardoso.quotesmvvm.data.remote.QuoteRemoteDataSource
import dev.cardoso.quotesmvvm.domain.QuoteRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject


class QuoteRepositoryImpl @Inject constructor(quoteDAO: QuoteDAO, var localDataSource: QuoteLocalDataSource,
                                             var remoteDataSource: QuoteRemoteDataSource
                                              ): QuoteRepository {

    override suspend  fun getQuotes(token: String): Flow<List<QuoteModel>> {
        Log.w("jdebug", "repository impl")
        val remoteQuotes =
        try {
            remoteDataSource.getQuotes(token)
        } catch (ex: Exception) {
            when (ex) {
                is NetworkErrorException -> throw ex
                else -> null
            }
        }
        val quotes = ArrayList<QuoteModel>()
        if (remoteQuotes != null) {
            remoteQuotes.collect {
                it?.forEach { quoteModel->
                    quotes.add(quoteModel)
                }
            }
            localDataSource.insertAll(quotes)
            Log.w("jdebug", "$quotes")
        }
        return (flow { emit (quotes) })
    }

    override suspend fun getQuoteRandom(): Flow<QuoteModel> {
        return  localDataSource.getQuoteRandom()
    }

    override suspend fun getQuote(quoteId: Int): Flow<QuoteModel> {
           return localDataSource.getQuote(quoteId)
    }

    override suspend fun addQuote(
        token: String,
        quoteRequest: QuoteRequest,
    ): Flow<AddQuoteResponse>? {
        return remoteDataSource.addQuote(token, quoteRequest)
    }

    override suspend fun editQuote(
        token: String,
        quoteRequest: QuoteRequest,
        id: String
    ): Flow<QuoteResponse>? {
        return remoteDataSource.editQuote(token, id, quoteRequest)
    }
    override suspend fun deleteQuote(
        token: String,
        id: Int
    ): Flow<QuoteResponse>? {
        return remoteDataSource.deleteQuote(token = token , id=id)
    }

    override suspend fun showQuote(token: String, id: Int): Flow<QuoteResponse>? {
        return remoteDataSource.showQuote(token = token , id=id)

    }


    override suspend fun getQuotesResponse(token: String): Flow<QuoteResponse>? {
        return remoteDataSource.getQuotesResponse(token = token)
    }
}