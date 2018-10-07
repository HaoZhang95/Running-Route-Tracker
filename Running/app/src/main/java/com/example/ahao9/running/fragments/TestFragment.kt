package com.example.ahao9.running.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.example.ahao9.running.R
import com.example.ahao9.running.R.id.*
import com.example.ahao9.running.activities.TestActivity
import com.example.ahao9.running.database.entity.TestUpload
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.test_layout.*
import org.jetbrains.anko.support.v4.startActivity


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 16:42 2018/9/30
 * @ Description：Build for Metropolia project
 */
class TestFragment : Fragment() {

    companion object {
        const val PICK_IMAGE_REQUEST = 1;
    }

    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    private var mUploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.test_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_choose_image.setOnClickListener { openFileChooer() }
        button_upload.setOnClickListener {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadFile();
            }
        }
        text_view_show_uploads.setOnClickListener { showAllPics() }

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
    }

    private fun showAllPics() {
        startActivity<TestActivity>()
    }

    private fun getFileExtension(uri: Uri): String {
        val cR = context!!.contentResolver
        val mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private fun uploadFile() {
        if (mImageUri != null) {
            val fileReference = mStorageRef.child(" ${System.currentTimeMillis()}.${getFileExtension(mImageUri!!)}");

            mUploadTask = fileReference.putFile(mImageUri!!).addOnSuccessListener {

                val handler = Handler()
                handler.postDelayed(Runnable {
                    progress_bar.progress = 0;

                }, 500)
                Toast.makeText(context, "Upload successful", Toast.LENGTH_LONG).show();
                val upload = TestUpload(edit_text_file_name.text.toString().trim(),
                        it.downloadUrl.toString());
                val uploadId = mDatabaseRef.push().key;
                mDatabaseRef.child(uploadId).setValue(upload);

            }.addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show();
            }.addOnProgressListener {
                val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                progress_bar.progress = progress.toInt()
            }
        } else {
            Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private fun openFileChooer() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.data != null) {
            mImageUri = data.data;
            Picasso.with(context).load(mImageUri).into(image_view);
        }
    }
}