package app.template.extension.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExamplePatch {

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void onPostFragmentViewCreated(final Object fragment, final View root) {
        if (!(root instanceof ViewGroup)) return;
        final ViewGroup viewGroup = (ViewGroup) root;
        final Context context = root.getContext();

        // Create the floating button
        final FrameLayout button = new FrameLayout(context);
        
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // Circular background with border matching light/dark mode UX
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        if (isDarkMode) {
            background.setColor(Color.parseColor("#1E293B")); // Dark slate
            background.setStroke(dpToPx(context, 1), Color.parseColor("#334155"));
        } else {
            background.setColor(Color.parseColor("#FFFFFF")); // Pure white
            background.setStroke(dpToPx(context, 1), Color.parseColor("#E2E8F0"));
        }
        button.setBackground(background);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(dpToPx(context, 6));
        }

        // Add "F" text with dynamic text color
        TextView textView = new TextView(context);
        textView.setText("F");
        textView.setTextColor(isDarkMode ? Color.parseColor("#F8FAFC") : Color.parseColor("#0F172A"));
        textView.setTextSize(20);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setGravity(Gravity.CENTER);
        button.addView(textView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Use reflection to construct CoordinatorLayout$LayoutParams dynamically with 16dp margin
        ViewGroup.LayoutParams lp = null;
        try {
            Class<?> clpClass = Class.forName("androidx.coordinatorlayout.widget.CoordinatorLayout$LayoutParams");
            lp = (ViewGroup.LayoutParams) clpClass.getConstructor(int.class, int.class).newInstance(dpToPx(context, 56), dpToPx(context, 56));
            clpClass.getField("gravity").set(lp, Gravity.BOTTOM | Gravity.END);
            clpClass.getMethod("setMargins", int.class, int.class, int.class, int.class).invoke(lp, 0, 0, dpToPx(context, 16), dpToPx(context, 16));
        } catch (Exception e) {
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(dpToPx(context, 56), dpToPx(context, 56));
            flp.gravity = Gravity.BOTTOM | Gravity.END;
            flp.setMargins(0, 0, dpToPx(context, 16), dpToPx(context, 16));
            lp = flp;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get post ID via reflection
                    Object bundleInfo = fragment.getClass().getMethod("p1").invoke(fragment);
                    Object targetPost = bundleInfo.getClass().getMethod("getPost").invoke(bundleInfo);
                    String id = (String) targetPost.getClass().getMethod("getId").invoke(targetPost);
                    
                    if (id != null) {
                        String articleUrl = "https://medium.com/p/" + id;
                        SharedPreferences prefs = context.getSharedPreferences("freedium_prefs", Context.MODE_PRIVATE);
                        String host = prefs.getString("freedium_host", "freedium-mirror.cfd");
                        String freediumUrl = "https://" + host + "/" + articleUrl;
                        
                        // Open the WebView dialog
                        FreediumWebViewDialog dialog = new FreediumWebViewDialog(context, freediumUrl);
                        dialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        viewGroup.addView(button, lp);
    }

    public static View wrapSettingsView(View composeView) {
        final Context context = composeView.getContext();
        
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Use same background color as settings screen
        rootLayout.setBackgroundColor(isDarkMode ? Color.BLACK : Color.WHITE);

        // Create the settings row layout
        RelativeLayout settingsRow = new RelativeLayout(context);
        settingsRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 72)
        ));
        settingsRow.setPadding(dpToPx(context, 16), 0, dpToPx(context, 16), 0);

        // Native selectable item background (ripple effect)
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        android.content.res.TypedArray ta = context.obtainStyledAttributes(attrs);
        android.graphics.drawable.Drawable ripple = ta.getDrawable(0);
        ta.recycle();
        settingsRow.setBackground(ripple);

        // Title text
        TextView title = new TextView(context);
        title.setText("Freedium Mirror Server");
        title.setTextSize(16);
        title.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        
        RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        titleLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleLp.topMargin = dpToPx(context, 14);
        settingsRow.addView(title, titleLp);

        // Subtitle text (shows current host)
        final TextView subtitle = new TextView(context);
        final SharedPreferences prefs = context.getSharedPreferences("freedium_prefs", Context.MODE_PRIVATE);
        String currentHost = prefs.getString("freedium_host", "freedium-mirror.cfd");
        subtitle.setText("Current: " + currentHost);
        subtitle.setTextSize(13);
        subtitle.setTextColor(isDarkMode ? Color.parseColor("#94A3B8") : Color.parseColor("#64748B"));
        
        RelativeLayout.LayoutParams subtitleLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        subtitleLp.bottomMargin = dpToPx(context, 14);
        settingsRow.addView(subtitle, subtitleLp);

        settingsRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] hosts = new String[]{
                        "freedium-mirror.cfd",
                        "freedium.cfd",
                        "freedium.net"
                };
                
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("Select Freedium Host");
                
                String current = prefs.getString("freedium_host", "freedium-mirror.cfd");
                int selectedIdx = -1;
                for (int i = 0; i < hosts.length; i++) {
                    if (hosts[i].equals(current)) {
                        selectedIdx = i;
                        break;
                    }
                }
                
                builder.setSingleChoiceItems(hosts, selectedIdx, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String chosenHost = hosts[which];
                        prefs.edit().putString("freedium_host", chosenHost).apply();
                        subtitle.setText("Current: " + chosenHost);
                        dialog.dismiss();
                    }
                });
                
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });

        // Add settings row
        rootLayout.addView(settingsRow);
        
        // Add a small divider matching theme divider color
        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(context, 1)
        ));
        divider.setBackgroundColor(isDarkMode ? Color.parseColor("#1E293B") : Color.parseColor("#E2E8F0"));
        rootLayout.addView(divider);

        // Add compose view
        LinearLayout.LayoutParams composeLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        );
        rootLayout.addView(composeView, composeLp);

        return rootLayout;
    }
}
