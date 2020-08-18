package com.luckyspinner.app.fragemnts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.luckyspinner.app.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotFragment extends Fragment {
    private final static int MINIMAL_SPIN_COUNT = 20;
    private final static int RANDOM_SPIN_COUNT = 60;
    private final static int MAXIMUM_ANIMATION_DURATION = 85;
    private final int[] result = {2, 1, 1, 2};
    private final int[] images;
    private final List<View> wheels;
    private final boolean[] isRotating;
    private final List<Integer> imagesForWheels;
    private Context context;


    public SlotFragment() {
        wheels = new ArrayList<>();
        imagesForWheels = new ArrayList<>();
        images = new int[]{
                R.drawable.gold,
                R.drawable.lemon,
                R.drawable.watermelon,
                R.drawable.plum,
                R.drawable.seven,
                R.drawable.cherry,
                R.drawable.jocker
        };
        isRotating = new boolean[]{false, false, false, false};
        imagesForWheels.add(images[0]);
        imagesForWheels.add(images[1]);
        imagesForWheels.add(images[2]);
        imagesForWheels.add(images[3]);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
        initWheels(view);
        initButton(view);
        setImagesToWheels(0);
        setImagesToWheels(1);
        setImagesToWheels(2);
        setImagesToWheels(3);
        return view;
    }

    private void initWheels(View rootView) {
        wheels.add(rootView.findViewById(R.id.wheel_01));
        wheels.add(rootView.findViewById(R.id.wheel_02));
        wheels.add(rootView.findViewById(R.id.wheel_03));
        wheels.add(rootView.findViewById(R.id.wheel_04));
    }

    private void setImagesToWheels(int wheelNumber) {
        int centerImage = result[wheelNumber];
        int topImage = centerImage - 1;
        if (topImage == -1) {
            topImage = imagesForWheels.size() - 1;
        }
        int bottomImage = centerImage + 1;
        if (bottomImage == imagesForWheels.size()) {
            bottomImage = 0;
        }
        ((ImageView) wheels.get(wheelNumber).findViewById(R.id.image_1))
                .setImageDrawable(ContextCompat.getDrawable(context, imagesForWheels.get(bottomImage)));
        ((ImageView) wheels.get(wheelNumber).findViewById(R.id.image_2))
                .setImageDrawable(ContextCompat.getDrawable(context, imagesForWheels.get(centerImage)));
        ((ImageView) wheels.get(wheelNumber).findViewById(R.id.image_3))
                .setImageDrawable(ContextCompat.getDrawable(context, imagesForWheels.get(topImage)));
    }

    private void initButton(View rootView) {
        //ImageButton spinButton = rootView.findViewById(R.id.spin_button);
        rootView.setOnClickListener(v -> {
            moveWheelDown(0, new Random().nextInt(RANDOM_SPIN_COUNT) + MINIMAL_SPIN_COUNT);
            moveWheelDown(1, new Random().nextInt(RANDOM_SPIN_COUNT) + MINIMAL_SPIN_COUNT);
            moveWheelDown(2, new Random().nextInt(RANDOM_SPIN_COUNT) + MINIMAL_SPIN_COUNT);
            moveWheelDown(3, new Random().nextInt(RANDOM_SPIN_COUNT) + MINIMAL_SPIN_COUNT);
        });
    }

    private void moveWheelDown(int wheelNumber, int spinCount) {
        float stepHeight = wheels.get(wheelNumber).findViewById(R.id.image_1).getHeight();
        isRotating[wheelNumber] = true;
        setImagesToWheels(wheelNumber);
        TranslateAnimation startAnimation =
                new TranslateAnimation(0
                        , 0
                        , 0
                        , stepHeight / 2f
                );
        startAnimation.setDuration(MAXIMUM_ANIMATION_DURATION - spinCount);
        startAnimation.setFillAfter(true);
        TranslateAnimation endAnimation =
                new TranslateAnimation(0
                        , 0
                        , - stepHeight / 2f
                        , 0
                );
        endAnimation.setDuration(MAXIMUM_ANIMATION_DURATION - spinCount);
        endAnimation.setFillAfter(true);
        startAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (result[wheelNumber] == imagesForWheels.size() - 1) {
                    result[wheelNumber] = 0;
                } else {
                    result[wheelNumber] = result[wheelNumber] + 1;
                }
                setImagesToWheels(wheelNumber);
                wheels.get(wheelNumber).setY(-stepHeight / 2f);
                wheels.get(wheelNumber).startAnimation(endAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        endAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (spinCount > 0) {
                    moveWheelDown(wheelNumber, spinCount - 1);
                } else {
                    isRotating[wheelNumber] = false;
                    checkResult();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        wheels.get(wheelNumber).startAnimation(startAnimation);
    }

    private void checkResult() {
        if (!isRotating[0] && !isRotating[1] && !isRotating[2]
                && imagesForWheels.get(result[0]).equals(imagesForWheels.get(result[1]))
                && imagesForWheels.get(result[1]).equals(imagesForWheels.get(result[2]))
                && imagesForWheels.get(result[2]).equals(imagesForWheels.get(result[3]))) {
            showWinAnimation();
            imagesForWheels.add(images[new Random().nextInt(images.length - 1)]);
        }
    }

    private void showWinAnimation() {
        List<ImageView> winImageViews = new ArrayList<>();
        winImageViews.add(wheels.get(0).findViewById(R.id.image_2));
        winImageViews.add(wheels.get(1).findViewById(R.id.image_2));
        winImageViews.add(wheels.get(2).findViewById(R.id.image_2));
        winImageViews.add(wheels.get(3).findViewById(R.id.image_2));
        float pivotX = winImageViews.get(0).getX() / 2f;
        float pivotY = winImageViews.get(0).getY() / 2f;
        ScaleAnimation winAnimation = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f,
                pivotX, pivotY);
        winAnimation.setDuration(MAXIMUM_ANIMATION_DURATION * 10);
        winAnimation.setFillAfter(false);
        for (ImageView image : winImageViews) {
            image.startAnimation(winAnimation);
        }
    }
}