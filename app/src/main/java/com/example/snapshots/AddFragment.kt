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
import android.widget.Toast
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {
    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReferences: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference
    private var RC_GALLERY = 18
    private var mPhotoSelectedUri: Uri? = null
    private val  PATH_SNAPSHOT = "snapshots"
    private lateinit var mContext: Context



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
        startActivityForResult(intent, RC_GALLERY)

    }

    private fun postSnapshot() {

        mBinding.progressBar.visibility = View.VISIBLE
        mStorageReferences.child(PATH_SNAPSHOT).child("my_photo")
        val reference = mStorageReferences.child(PATH_SNAPSHOT).child("my_photo")
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
                    Toast.makeText(mContext,"Instantanea publicada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(mContext,"Ocurrio un error, intentelo mas tarde", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapshot(){

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RC_GALLERY){
                mPhotoSelectedUri = data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }

}