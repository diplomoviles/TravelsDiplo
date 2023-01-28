package com.amaurypm.travelsdiplo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.amaurypm.travelsdiplo.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding : ActivityMainBinding

    private lateinit var  authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var accessTokenTracker: AccessTokenTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callbackManager = CallbackManager.Factory.create()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setPermissions("email", "public_profile", "user_friends")



        binding.loginButton.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

            override fun onSuccess(result: LoginResult) {
                manejaTokenAcceso(result?.accessToken)
            }

        })



        authStateListener = FirebaseAuth.AuthStateListener {firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user!=null)
                actualizaUI(user)
            else
                actualizaUI(null)
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

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    private fun manejaTokenAcceso(accessToken: AccessToken?) {
        val authCredential = accessToken?.let { FacebookAuthProvider.getCredential(it.token) }
        if(authCredential != null){
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener {
                if(it.isSuccessful){
                    val user = firebaseAuth.currentUser
                    actualizaUI(user)
                }
                else{
                    //Manejar el error
                }
            }
        }
    }

    private fun actualizaUI(user: FirebaseUser?) {
        if(user!=null){
            binding.tvNombrePerfil.text = user.displayName
            if(user.photoUrl!=null){
                var photoUrl = user.photoUrl.toString()
                photoUrl = "$photoUrl?access_token=${AccessToken.getCurrentAccessToken()?.token}&type=large"
                Glide.with(this).load(photoUrl).into(binding.ivImagenPerfil)
                binding.ivTravel.visibility = View.INVISIBLE
            }
        }else{
            binding.tvNombrePerfil.text = ""
            binding.ivImagenPerfil.setImageResource(0)
            binding.ivTravel.visibility = View.VISIBLE
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


}