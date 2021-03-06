package com.abduqodirov.invitex.firestore

import com.abduqodirov.invitex.models.Mehmon
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder

object CloudFirestoreRepo {

    var weddingId: String = ""
    var username: String = ""

    private val weddingsCollection by lazy {
        FirebaseFirestore.getInstance()
            .collection("weddings")
    }

    fun initializeFireStore(
        cardAmount: Int,
        username: String,
        connectedClickListener: ConnectedClickListener
    ) {

        this.username = username

        val wedding = hashMapOf(
            "card_amout" to cardAmount,
            "members" to arrayListOf(
                username
            )
        )

        weddingsCollection
            .add(wedding)
            .addOnSuccessListener {
                connectedClickListener.onConnect(weddingId = it.id)
                weddingId = it.id
            }

    }

    fun joinToExistingWedding(
        username: String,
        weddingIdArg: String,
        connetedClickListener: ConnectedClickListener
    ) {
        this.username = username

        weddingsCollection.document(weddingIdArg)
            .update("members", FieldValue.arrayUnion(username))
            .addOnSuccessListener {
                connetedClickListener.onConnect(weddingId = weddingIdArg)
                weddingId = weddingIdArg
            }
            .addOnFailureListener {
            }
    }


    fun sendToFireStore(mehmon: Mehmon) {
        if (isFirestoreConnected()) {
            weddingsCollection.document(weddingId).collection("${username}-${mehmon.toifa}")
                .document("${mehmon.mehmonId}")
                .set(
                    Mehmon(
                        mehmonId = mehmon.mehmonId,
                        caller = username,
                        ism = mehmon.ism,
                        isAytilgan = mehmon.isAytilgan,
                        toifa = mehmon.toifa
                    )
                )
        }

    }

    fun updateItemOnFirestore(mehmon: Mehmon, completedClickListener: CompletedClickListener) {

        if (isFirestoreConnected()) {
            weddingsCollection.document(weddingId)
                .collection("${getCallerOfMehmon(mehmon)}-${mehmon.toifa}")
                .document("${mehmon.mehmonId}")
                .set(mehmon)
                .addOnSuccessListener {
                    completedClickListener.onFinished(isSuccessful = true, result = "Result is $it")
                }
                .addOnFailureListener {
                    completedClickListener.onFinished(
                        isSuccessful = false,
                        result = "Exception: $it"
                    )
                }
        }
    }

    fun isThereWeddingWithPassedId(
        passedId: String,
        completedClickListener: CompletedClickListener
    ) {

        weddingsCollection.document(passedId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completedClickListener.onFinished("Qaytdi", task.result!!.exists())
                } else {
                    completedClickListener.onFinished("Qaytdi", false)
                }

            }


    }

    fun isUsernameAvailable(
        passedId: String,
        passedUsername: String,
        completedClickListener: CompletedClickListener
    ) {

        weddingsCollection.document(passedId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val weddingDoc = task.result!!.data!!.toSortedMap()
                    val members = weddingDoc["members"] as List<String>

                    if (members.contains(passedUsername)) {
                        completedClickListener.onFinished("Bu username band", false)
                    } else {
                        completedClickListener.onFinished("Finished", true)
                    }
                } else {
                    completedClickListener.onFinished("Xatolik yuz berdi", false)
                }
            }
    }

    fun isFirestoreConnected() = weddingId.isNotEmpty() && username.isNotEmpty()

    fun deleteMehmonFromFirestore(mehmon: Mehmon, completedClickListener: CompletedClickListener) {
        if (isFirestoreConnected()) {
            weddingsCollection.document(weddingId)
                .collection("${getCallerOfMehmon(mehmon)}-${mehmon.toifa}")
                .document("${mehmon.mehmonId}")
                .delete()
                .addOnSuccessListener {
                    completedClickListener.onFinished(isSuccessful = true, result = "Result is $it")
                }
                .addOnFailureListener {
                    completedClickListener.onFinished(
                        isSuccessful = false,
                        result = "Exception: $it"
                    )
                }
        }
    }

    private fun getCallerOfMehmon(mehmon: Mehmon): String {
        return if (mehmon.caller == "local") {
            username
        } else {
            mehmon.caller
        }
    }

}

class ConnectedClickListener(val connectedClickListener: (weddingId: String) -> Unit) {
    fun onConnect(weddingId: String) = connectedClickListener(weddingId)
}

class CompletedClickListener(val completedClickListener: (result: String, isSuccessful: Boolean) -> Unit) {
    fun onFinished(result: String, isSuccessful: Boolean) =
        completedClickListener(result, isSuccessful)
}