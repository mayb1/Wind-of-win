package wind.win.com;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

import io.michaelrocks.paranoid.Obfuscate;
import wind.win.com.databinding.FragmentWowGameBinding;
@Obfuscate
public class WowGameFragment extends Fragment {

    private FragmentWowGameBinding binding;

    private SharedPreferences wowSaveData;

    private final static int START_POINTS = 7500, START_BET = 150;

    private int wowGameScore, wowGameBet = START_BET;

    private final int[] wowCardsResources = {
            R.drawable.clubs_a,
            R.drawable.clubs_02,
            R.drawable.clubs_03,
            R.drawable.clubs_04,
            R.drawable.clubs_05,
            R.drawable.clubs_06,
            R.drawable.clubs_07,
            R.drawable.clubs_08,
            R.drawable.clubs_09,
            R.drawable.clubs_10,
            R.drawable.clubs_j,
            R.drawable.clubs_k,
            R.drawable.clubs_q,
            R.drawable.diamonds_a,
            R.drawable.diamonds_02,
            R.drawable.diamonds_03,
            R.drawable.diamonds_04,
            R.drawable.diamonds_05,
            R.drawable.diamonds_06,
            R.drawable.diamonds_07,
            R.drawable.diamonds_08,
            R.drawable.diamonds_09,
            R.drawable.diamonds_10,
            R.drawable.diamonds_j,
            R.drawable.diamonds_k,
            R.drawable.diamonds_q,
            R.drawable.hearts_a,
            R.drawable.hearts_02,
            R.drawable.hearts_03,
            R.drawable.hearts_04,
            R.drawable.hearts_05,
            R.drawable.hearts_06,
            R.drawable.hearts_07,
            R.drawable.hearts_08,
            R.drawable.hearts_09,
            R.drawable.hearts_10,
            R.drawable.hearts_j,
            R.drawable.hearts_k,
            R.drawable.hearts_q,
            R.drawable.spades_a,
            R.drawable.spades_02,
            R.drawable.spades_03,
            R.drawable.spades_04,
            R.drawable.spades_05,
            R.drawable.spades_06,
            R.drawable.spades_07,
            R.drawable.spades_08,
            R.drawable.spades_09,
            R.drawable.spades_10,
            R.drawable.spades_j,
            R.drawable.spades_k,
            R.drawable.spades_q,
    };

    private int[] wowCardsValue = {11,2,3,4,5,6,7,8,9,10,10,10,10, 11,2,3,4,5,6,7,8,9,10,10,10,10,
            11,2,3,4,5,6,7,8,9,10,10,10,10, 11,2,3,4,5,6,7,8,9,10,10,10,10};

    private ImageView[] wowCards;

    private int wowBetChanger = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWowGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wowSaveData = Objects.requireNonNull(getActivity()).getSharedPreferences("wowSaveData", Context.MODE_PRIVATE);
        wowGameScore = wowSaveData.getInt("wowSaveData", START_POINTS);

        binding.tvWowGameScoreText.setText(String.valueOf(wowGameScore));
        binding.tvWowGameBetText.setText(String.valueOf(wowGameBet));


        wowCards = new ImageView[]{binding.ivWowPlayerCard, binding.ivWowDealerCard};

        binding.ivWowBetButton.setOnClickListener(v -> {
            int[] wowBetValue = new int[] {150, 250, 350, 450, 550};

            try {

                if ((wowBetChanger + 1) > wowBetValue.length){
                    wowBetChanger = 0;
                } else {
                    wowBetChanger++;
                }

                binding.tvWowGameBetText.setText(String.valueOf(wowBetValue[wowBetChanger]));
                wowGameBet = wowBetValue[wowBetChanger];
            }  catch (Exception ignored){}

        });



        binding.bDealWowCards.setOnClickListener(v -> {
            dealCards();
        });

    }

    @SuppressLint("SetTextI18n")
    public void changePoints(int value){
        wowGameScore += value;
        binding.tvWowGameScoreText.setText(String.valueOf(wowGameScore));
        wowSaveData.edit()
                .putInt("wowSaveData", wowGameScore)
                .apply();

    }


    public void dealCards() {
        binding.bDealWowCards.setEnabled(false);
        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long l) {
                for (ImageView wowCard : wowCards) {
                    int random = (int) (Math.random() * wowCardsResources.length);
                    wowCard.setImageResource(wowCardsResources[random]);
                    wowCard.setTag(wowCardsValue[random]);
                }
            }

            @Override
            public void onFinish() {
                int playerCard = 0;
                int dealerCard = 0;
                int gameRes;
                playerCard = Integer.parseInt(binding.ivWowPlayerCard.getTag().toString());
                dealerCard = Integer.parseInt(binding.ivWowDealerCard.getTag().toString());

                if (dealerCard > playerCard){
                    gameRes = -wowGameBet;
                    Toast.makeText(getContext(), "Dealer win", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Player win" + wowGameBet, Toast.LENGTH_SHORT).show();
                    gameRes = +wowGameBet;
                }
                changePoints(gameRes);

                binding.bDealWowCards.setEnabled(true);
            }
        }.start();
    }




}