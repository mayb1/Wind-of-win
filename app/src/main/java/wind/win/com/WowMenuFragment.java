package wind.win.com;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.michaelrocks.paranoid.Obfuscate;
import wind.win.com.databinding.FragmentWowMenuBinding;
@Obfuscate
public class WowMenuFragment extends Fragment {

    private FragmentWowMenuBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWowMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.bWowExit.setOnClickListener(v01 -> requireActivity().finish());
        binding.bWowPrivPol.setOnClickListener(v02 -> Navigation.findNavController(requireView()).navigate(R.id.navigation_wowPrivacyPolicy));
        binding.bWowGame.setOnClickListener(v03 -> Navigation.findNavController(requireView()).navigate(R.id.navigation_wowGame));
    }

}