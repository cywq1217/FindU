package com.example.findu.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.Auth

object SupabaseClient {
    private const val SUPABASE_URL = "https://pmlszeaqvwdterkbkngw.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_IrotDK4gzhX3jBtOZrsO3w_jujPZEgX"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth)
    }
}