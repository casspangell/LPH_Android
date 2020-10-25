package org.lovepeaceharmony.androidapp.utility

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import org.lovepeaceharmony.androidapp.R
import java.lang.ref.WeakReference


/**
 * GoogleSingIn
 * Created by Naveen Kumar M on 18/07/17.
 */
class GoogleSingIn(private val context: Context) : GoogleApiClient.OnConnectionFailedListener {

    private val mGoogleApiClient: GoogleApiClient
    // [START declare_auth]
    private val mAuth: FirebaseAuth
    @VisibleForTesting
    private var mProgressDialog: ProgressDialog? = null

    init {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // [END config_signin]

        mGoogleApiClient = GoogleApiClient.Builder(context)
                .enableAutoManage(context as AppCompatActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        //        signIn();


    }

    fun checkUser() {
        LPHLog.d("Coming to Check User")
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }


    fun currentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    private fun signIn() {
        LPHLog.d("Coming to calling Sing in 2")
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        (context as AppCompatActivity).startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    fun activityResult(requestCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN && data != null) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult<ApiException>(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                LPHLog.d("Google sign in failed" + e)
                // ...
            }


            /*GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
//                Toast.makeText(context, "Authentication failed. Try Again!",
//                        Toast.LENGTH_SHORT).show();
                ((AppCompatActivity) context).finish();

            }*/
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        LPHLog.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        showProgressDialog()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context as AppCompatActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        LPHLog.d(TAG, "signInWithCredential:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        LPHLog.d(TAG, "onConnectionFailed:" + connectionResult)
        Toast.makeText(context, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    //    public void signOut() {
    //        // Firebase sign out
    //        mAuth.signOut();
    //
    //
    //
    ////        // Google sign out
    ////        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
    ////                new ResultCallback<Status>() {
    ////                    @Override
    ////                    public void onResult(@NonNull Status status) {
    //////                        updateUI(null);
    ////                    }
    ////                });
    //    }


    private fun updateUI(user: FirebaseUser?) {
        LPHLog.d("Coming to Update UI")
        hideProgressDialog()
        if (user != null) {
            LPHLog.d("USerEmail : " + user.email + " text")
            LPHLog.d("UserID : " + user.uid + " text")

            val account : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                val personName = account.displayName
                val personGivenName = account.givenName
                val personFamilyName = account.familyName
                val personEmail: String? = account.email
                val personId = account.id
                val personPhoto = account.photoUrl.toString()

                val weakReferenceContext: WeakReference<Context> = WeakReference(context)
                val registerAsync = Helper.RegisterAsync(weakReferenceContext, personEmail!!, personId!!, personPhoto, personName!!, false, Constants.LoginType.Google)
                registerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        } else {
            LPHLog.d("Coming to SingIn 1")
            signIn()
        }
    }

    private fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setMessage(context.getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }


    fun pause() {
        mGoogleApiClient.stopAutoManage(context as AppCompatActivity)
        mGoogleApiClient.disconnect()
    }

    companion object {
        // [END declare_auth]
        val RC_SIGN_IN = 9001
        private val TAG = "GoogleSingIn"
    }

}
