package com.example.shuffle_showdown.ui.play

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shuffle_showdown.databinding.FragmentPlayBinding
import com.example.shuffle_showdown.ui.play.multiplayer.HostLobbyActivity
import com.example.shuffle_showdown.ui.play.singleplayer.SelectDeckActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val playViewModel =
            ViewModelProvider(this).get(PlayViewModel::class.java)

        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnStudy.setOnClickListener {
            val intent = Intent(requireActivity(), SelectDeckActivity::class.java)
            intent.putExtra("mode", "study")
            startActivity(intent)
        }

        binding.btnSingleplayer.setOnClickListener {
            val intent = Intent(requireActivity(), SelectDeckActivity::class.java)
            intent.putExtra("mode", "single")
            startActivity(intent)
        }

        binding.btnMultiplayer.setOnClickListener {
            val intent = Intent(requireActivity(), HostLobbyActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}