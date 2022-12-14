package com.example.snapshots

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {
    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReferences: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference
    private var mPhotoSelectedUri: Uri? = null
    private val  PATH_SNAPSHOT = "snapshots"
    private lateinit var mContext: Context

    private val galeryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
                mPhotoSelectedUri = it.data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return  mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        mBinding.btnPost.setOnClickListener {
            postSnapshot()
        }

        mBinding.btnSelected.setOnClickListener {
            openGallery()
        }

        mStorageReferences = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galeryResult.launch(intent)

    }

    private fun postSnapshot() {

        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val reference = mStorageReferences.child(PATH_SNAPSHOT).child(FirebaseAuth.getInstance().currentUser!!.uid).child(key)
        if (mPhotoSelectedUri != null) {
            reference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }
                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Toast.makeText(activity,"Instantanea publicada", Toast.LENGTH_SHORT).show()

                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key, it.toString(), mBinding.etTitle.text.toString().trim())

                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                        mBinding.imgPhoto.setImageURI(null)
                        hideKeyboard()


                    }
                }
                .addOnFailureListener{
                    Toast.makeText(activity,"Ocurrio un error, intentelo mas tarde", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapshot(key: String, url: String, title: String){
        val snapshot = Snapshot(title = title, photoUrl = url )
        mDatabaseReference.child(key).setValue(snapshot)

    }
    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}