package com.gucodero.biometric_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.gucodero.biometric_compose.entities.AllowedAuthenticators
import com.gucodero.biometric_compose.entities.CiphertextWrapper
import com.gucodero.biometric_compose.entities.DecryptionResult
import com.gucodero.biometric_compose.entities.EncryptionResult
import com.gucodero.biometric_compose.utils.isBiometricPromptIsAvailable
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenceUtil(baseContext)
        setContent {
            Screen(
                prefs = prefs,
                toast = ::toast,
            )
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun Screen(
    prefs: PreferenceUtil,
    toast: (String) -> Unit
){
    val isBiometricPromptAvailable = isBiometricPromptIsAvailable(
        allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colors.background
            )
            .padding(
                16.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!isBiometricPromptAvailable){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.biometric_not_available)
                )
            }
        } else {
            val biometricPrompt = rememberBiometricPrompt(
                allowedAuthenticators = AllowedAuthenticators.BIOMETRIC_STRONG,
                title = stringResource(id = R.string.biometric_prompt_title),
                subtitle = stringResource(id = R.string.biometric_prompt_subtitle),
                description = stringResource(id = R.string.biometric_prompt_description),
                negativeButtonText = stringResource(id = R.string.biometric_prompt_negative_button),
                confirmationRequired = false
            )
            val coroutine = rememberCoroutineScope()
            var key by remember {
                mutableStateOf(prefs.username)
            }
            var value by remember {
                mutableStateOf("")
            }

            var savedCipher: CiphertextWrapper? = null
            if (prefs.useFingerprint) {
                savedCipher = CiphertextWrapper(prefs.cipherText, prefs.initializationVector)
                LaunchedEffect(key1 = Unit) {
                    coroutine.launch {
                        biometricPrompt.decryption(
                            key = prefs.username,
                            value = savedCipher!!
                        ).collect {
                            when (it) {
                                is DecryptionResult.Success -> {
                                    toast("SUCCESS: ${it.value}")
                                    value = it.value
                                }

                                is DecryptionResult.Failed -> {
                                    toast("FAILED")
                                }

                                is DecryptionResult.Error -> {
                                    toast("ERROR: ${it.errorCode} - ${it.description}")
                                }

                                is DecryptionResult.Cancel -> {
                                    toast("CANCEL")
                                }

                                is DecryptionResult.NotAvailable -> {
                                    toast("NotAvailable")
                                }

                                is DecryptionResult.CipherException -> {
                                    toast(it.exception.toString())
                                }
                            }
                        }
                    }
                }
            }
            var cipherValue: CiphertextWrapper? by remember {
                mutableStateOf(savedCipher)
            }
            if(cipherValue == null){
                TextField(
                    value = key,
                    onValueChange = {
                        key = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "KEY")
                    }
                )
                TextField(
                    value = value,
                    onValueChange = {
                        value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    label = {
                        Text(text = "VALUE")
                    }
                )
                Button(
                    onClick = {
                        coroutine.launch {
                            biometricPrompt.encryption(
                                key = key,
                                value = value
                            ).collect {
                                when(it){
                                    is EncryptionResult.Success -> {
                                        toast("SUCCESS")
                                        cipherValue = it.cipherValue
                                        /*prefs.username = key
                                        prefs.cipherText = it.cipherValue.ciphertext
                                        prefs.initializationVector = it.cipherValue.initializationVector
                                        prefs.useFingerprint = true*/
                                        saveCredentials(prefs, key, it.cipherValue)
                                    }
                                    is EncryptionResult.Failed -> {
                                        toast("FAILED")
                                    }
                                    is EncryptionResult.Error -> {
                                        toast("ERROR: ${it.errorCode} - ${it.description}")
                                    }
                                    is EncryptionResult.Cancel -> {
                                        toast("CANCEL")
                                    }
                                    is EncryptionResult.NotAvailable -> {
                                        toast("NotAvailable")
                                    }
                                    is EncryptionResult.CipherException -> {
                                        toast(it.exception.toString())
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    enabled = key.isNotEmpty() && value.isNotEmpty()
                ) {
                    Text(text = "Encryption")
                }
            }
            else {
                TextField(
                    value = key,
                    onValueChange = {
                        key = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "KEY")
                    }
                )
                Button(
                    onClick = {
                        coroutine.launch {
                            biometricPrompt.decryption(
                                key = key,
                                value = cipherValue!!
                            ).collect {
                                when(it){
                                    is DecryptionResult.Success -> {
                                        toast("SUCCESS: ${it.value}")
                                    }
                                    is DecryptionResult.Failed -> {
                                        toast("FAILED")
                                    }
                                    is DecryptionResult.Error -> {
                                        toast("ERROR: ${it.errorCode} - ${it.description}")
                                    }
                                    is DecryptionResult.Cancel -> {
                                        toast("CANCEL")
                                    }
                                    is DecryptionResult.NotAvailable -> {
                                        toast("NotAvailable")
                                    }
                                    is DecryptionResult.CipherException -> {
                                        toast(it.exception.toString())
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    enabled = key.isNotEmpty()
                ) {
                    Text(text = "Decryption")
                }
                Button(
                    onClick = {
                        cipherValue = null
                        key = ""
                        value = ""
                        resetCredentials(prefs)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text(text = "Reset")
                }
            }
        }
    }
}

private fun saveCredentials(
    prefs: PreferenceUtil,
    key: String,
    cipherValue: CiphertextWrapper
) {
    prefs.username = key
    prefs.cipherText = cipherValue.ciphertext
    prefs.initializationVector = cipherValue.initializationVector
    prefs.useFingerprint = true
}

private fun resetCredentials(prefs: PreferenceUtil) {
    prefs.username = ""
    prefs.useFingerprint = false
}