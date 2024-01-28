package com.example.shuffle_showdown.ui.cards

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shuffle_showdown.R
import com.example.shuffle_showdown.cueCard.CueCard
import com.example.shuffle_showdown.cueCard.CueCardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CueCardDetailActivity : AppCompatActivity() {

    private val viewModel: CueCardViewModel by viewModels()
    private var isEditing = false
    private lateinit var cueCard: CueCard

    private lateinit var editTerm: EditText
    private lateinit var editDefinition: EditText
    private lateinit var editFakeAnswer1: EditText
    private lateinit var editFakeAnswer2: EditText
    private lateinit var editFakeAnswer3: EditText

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    private lateinit var saveBtn: Button
    private lateinit var deleteBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cue_card_detail)

        editTerm = findViewById(R.id.editTerm)
        editDefinition = findViewById(R.id.editDefinition)
        editFakeAnswer1 = findViewById(R.id.editFakeAnswer1)
        editFakeAnswer2 = findViewById(R.id.editFakeAnswer2)
        editFakeAnswer3 = findViewById(R.id.editFakeAnswer3)
        radioGroup = findViewById(R.id.switches)
        saveBtn = findViewById(R.id.edit)
        deleteBtn = findViewById(R.id.delete)

        setUpRadioButton()

        setUpUI()

        saveBtn.setOnClickListener {
            if (isEditing) {
                updateCueCard()
            } else {
                createCueCard()
            }
        }

        deleteBtn.setOnClickListener {
            deleteCueCard()
        }
    }

    private fun setUpRadioButton() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioButton = findViewById(checkedId)
            if (radioButton.text == "Single Input") {
                editFakeAnswer1.isFocusable = false
                editFakeAnswer1.isFocusableInTouchMode = false
                editFakeAnswer2.isFocusable = false
                editFakeAnswer2.isFocusableInTouchMode = false
                editFakeAnswer3.isFocusable = false
                editFakeAnswer3.isFocusableInTouchMode = false
            } else {
                editFakeAnswer1.isFocusable = true
                editFakeAnswer1.isFocusableInTouchMode = true
                editFakeAnswer2.isFocusable = true
                editFakeAnswer2.isFocusableInTouchMode = true
                editFakeAnswer3.isFocusable = true
                editFakeAnswer3.isFocusableInTouchMode = true
            }
        }
    }

    private fun setUpUI() {
        // Handling Intent data
        val type = intent.getStringExtra("type")
        if (type == "view") {
            isEditing = true
            deleteBtn.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cueCard = intent.getParcelableExtra("cue card", CueCard::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                cueCard = intent.getParcelableExtra("cue card")!!
            }

            editTerm.setText(cueCard.term)
            editDefinition.setText(cueCard.answer)
            if (cueCard.isMultipleChoice == true) {
                editFakeAnswer1.setText(cueCard.fakeAnswer1)
                editFakeAnswer2.setText(cueCard.fakeAnswer2)
                editFakeAnswer3.setText(cueCard.fakeAnswer3)

                radioGroup.check(R.id.mcButton)
            }

        } else {
            isEditing = false
            deleteBtn.visibility = View.GONE
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
    }

    private fun createCueCard() {
        // Save cue card into database if input fields are not empty
        val deckId = intent.getStringExtra("deckId")

        val checkedId = radioGroup.checkedRadioButtonId
        radioButton = findViewById(checkedId)

        if (editTerm.text.isNotEmpty() && editDefinition.text.isNotEmpty()) {
            val cueCard = CueCard(
                id = "",
                term = editTerm.text.toString(),
                answer = editDefinition.text.toString(),
                deckId = deckId,
                isMultipleChoice = false
            )

            if (radioButton.text == "Multiple Choice") {
                updateMultipleChoiceCueCard(cueCard)
            } else {
                // Single Input
                addCueCardAndFinish(cueCard)
            }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCueCardAndFinish(cueCard: CueCard) {
        viewModel.addCueCard(cueCard)
        Toast.makeText(this, "Successfully created cue card.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateCueCard() {
        val checkedId = radioGroup.checkedRadioButtonId
        radioButton = findViewById(checkedId)

        if (editTerm.text.isNotEmpty() && editDefinition.text.isNotEmpty()) {
            val cueCard = CueCard(
                id = cueCard.id,
                term = editTerm.text.toString(),
                answer = editDefinition.text.toString(),
                deckId = cueCard.deckId,
                isMultipleChoice = false
            )
            if (radioButton.text == "Multiple Choice") {
                updateMultipleChoiceCueCard(cueCard)
            } else {
                // Single Input
                updateCueCardAndFinish(cueCard)
            }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to determine if text fields are empty for MC
    private fun updateMultipleChoiceCueCard(cueCard: CueCard) {
        if (editFakeAnswer1.text.isNotEmpty() &&
            editFakeAnswer2.text.isNotEmpty() &&
            editFakeAnswer3.text.isNotEmpty()) {
            cueCard.isMultipleChoice = true
            cueCard.fakeAnswer1 = editFakeAnswer1.text.toString()
            cueCard.fakeAnswer2 = editFakeAnswer2.text.toString()
            cueCard.fakeAnswer3 = editFakeAnswer3.text.toString()

            val type = intent.getStringExtra("type")
            if (type == "view") {
                updateCueCardAndFinish(cueCard)
            } else {
                addCueCardAndFinish(cueCard)
            }
        } else {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCueCardAndFinish(cueCard: CueCard) {
        viewModel.updateCueCard(cueCard)
        Toast.makeText(this, "Successfully updated cue card.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteCueCard() {
        // Delete Cue Card, then go back to deleted Cue Card's set list
        viewModel.deleteCueCard(
            CueCard(
                id = cueCard.id,
                term = editTerm.text.toString(),
                answer = editDefinition.text.toString()
            )
        )
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Close the activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}