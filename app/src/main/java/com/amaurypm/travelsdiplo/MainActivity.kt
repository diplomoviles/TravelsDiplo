package com.amaurypm.travelsdiplo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amaurypm.travelsdiplo.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var accessTokenTracker: AccessTokenTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //Instanciando el callback manager y el objeto de firebaseauth
        callbackManager = CallbackManager.Factory.create()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setPermissions("email", "public_profile")

        binding.loginButton.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onCancel() {
                Toast.makeText(
                    this@MainActivity,
                    "Error al ingresar. Por favor instala la app de Facebook e inicia sesión desde ahí",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al ingresar. Por favor instala la app de Facebook e inicia sesión desde ahí",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }
        })

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user != null){
                updateUI(user)
            }else{
                updateUI(null)
            }
        }

        accessTokenTracker = object: AccessTokenTracker(){
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if(currentAccessToken == null){
                    firebaseAuth.signOut()
                }
            }

        }
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        val authCredential = accessToken.let { accessToken ->
            FacebookAuthProvider.getCredential(accessToken.token)
        }
        //Nos registramos en firebase con ese token de acceso de facebook
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val user = firebaseAuth.currentUser
                updateUI(user)
            }else{
                //Manejamos el error
                Log.d("APPLOG", "Error: ${task.exception.toString()}")
                Toast.makeText(
                    this,
                    "Error al ingresar",
                    Toast.LENGTH_SHORT
                ).show()
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            binding.tvNombrePerfil.text = user.displayName
            if(user.photoUrl != null){
                var photoUrl = user.photoUrl.toString()
                photoUrl = "$photoUrl?access_token=${AccessToken.getCurrentAccessToken()?.token}&type=large"
                binding.ivTravel.visibility = View.INVISIBLE
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.ivImagenPerfil)
            }
        }else{
            binding.tvNombrePerfil.text = ""
            binding.ivImagenPerfil.setImageResource(0)
            binding.ivTravel.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}