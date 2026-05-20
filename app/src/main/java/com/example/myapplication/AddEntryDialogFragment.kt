package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.DialogAddEntryBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddEntryDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogAddEntryBinding? = null
    private val binding get() = _binding!!

    private val dateFormatter = SimpleDateFormat("MMM dd", Locale.ENGLISH)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

        binding.etDate.setText(dateFormatter.format(Date()))

        binding.etDate.setOnClickListener { showDatePicker() }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener { saveEntry() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            cal.set(year, month, day)
            binding.etDate.setText(dateFormatter.format(cal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveEntry() {
        val date = binding.etDate.text.toString().trim()
        val revenue = binding.etRevenue.text.toString().toFloatOrNull()
        val conversion = binding.etConversion.text.toString().toFloatOrNull()
        val retention = binding.etRetention.text.toString().toFloatOrNull()
        val forecast = binding.etForecastAccuracy.text.toString().toFloatOrNull()
        val efficiency = binding.etEfficiency.text.toString().toFloatOrNull()
        val savings = binding.etSavings.text.toString().toFloatOrNull()
        val lowRisk = binding.etLowRisk.text.toString().toIntOrNull()
        val medRisk = binding.etMedRisk.text.toString().toIntOrNull()
        val highRisk = binding.etHighRisk.text.toString().toIntOrNull()

        if (revenue == null || conversion == null || retention == null || forecast == null ||
            efficiency == null || savings == null || lowRisk == null || medRisk == null || highRisk == null) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val entry = WeeklyEntry(
            date = date,
            revenue = revenue,
            conversion = conversion,
            retention = retention,
            forecastAccuracy = forecast,
            efficiency = efficiency,
            potentialSavings = savings,
            lowRisk = lowRisk,
            mediumRisk = medRisk,
            highRisk = highRisk
        )

        ViewModelProvider(requireActivity())[SharedViewModel::class.java].addEntry(entry)
        Toast.makeText(requireContext(), "Entry saved!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
