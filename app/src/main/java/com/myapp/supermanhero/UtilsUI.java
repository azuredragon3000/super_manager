package com.myapp.supermanhero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Calendar;

public class UtilsUI {
    public static int darker(int color,double factor){
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int)(r*factor),0),
                Math.max((int)(g*factor),0),
                Math.max((int)(b*factor),0)
                );

    }
    public static Drawer setNavigationDrawer(
            Activity activity,
            final Context context, Toolbar toolbar,
            final AppAdapter appAdapter,
            final AppAdapter appSystemAdapter,
            final AppAdapter appFavoriteAdapter,
            final AppAdapter appHiddenAdapter,
            final RecyclerView recyclerView
    ){

        String apps,system,favorite,hidden;

        final String loadingLabel = "...";
        int header;
        if(getDayorNight() == 1){
           header = R.drawable.header_day;
        }else{
            header = R.drawable.header_night;
        }

        if(appAdapter != null){
            apps = Integer.toString(appAdapter.getItemCount());
        }else{
            apps = loadingLabel;
        }
        if(appSystemAdapter != null){
            system = Integer.toString(appSystemAdapter.getItemCount());
        }else{
            system = loadingLabel;
        }
        if(appFavoriteAdapter != null){
            favorite = Integer.toString(appFavoriteAdapter.getItemCount());
        }else{
            favorite = loadingLabel;
        }
        if(appHiddenAdapter != null){
            hidden = Integer.toString(appHiddenAdapter.getItemCount());
        }else{
            hidden = loadingLabel;
        }

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(header)
                .build();
        Integer badgeColor = ContextCompat.getColor(context,R.color.divider);
        BadgeStyle badgeStyle = new BadgeStyle(badgeColor,badgeColor).withTextColor(Color.GRAY);

        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(activity);
        drawerBuilder.withToolbar(toolbar);
        drawerBuilder.withAccountHeader(headerResult);

        //drawerBuilder.withStatusBarColor(UtilsUI.darker(Color.GREEN,0.8));

        drawerBuilder.addDrawerItems(
                new PrimaryDrawerItem().withName(
                        context.getResources().getString(R.string.action_apps)).
                        withIcon(GoogleMaterial.Icon.gmd_phone_android).withBadge(apps).withBadgeStyle(badgeStyle)
                        .withIdentifier(1),
                new PrimaryDrawerItem().withName(
                        context.getResources().getString(R.string.action_system_apps))
                        .withIcon(GoogleMaterial.Icon.gmd_android).withBadge(system).withBadgeStyle(badgeStyle)
                        .withIdentifier(2),
                new DividerDrawerItem(),
                new PrimaryDrawerItem().withName(
                                context.getResources().getString(R.string.action_favorites))
                        .withIcon(GoogleMaterial.Icon.gmd_star).withBadge(favorite).withBadgeStyle(badgeStyle)
                        .withIdentifier(3),
                new DividerDrawerItem(),
                new PrimaryDrawerItem().withName(context.getResources().getString(R.string.action_hidden_apps))
                        .withIcon(GoogleMaterial.Icon.gmd_visibility_off).withBadge(hidden).withBadgeStyle(badgeStyle)
                        .withIdentifier(4),
                new DividerDrawerItem(),
                new SecondaryDrawerItem().withName(
                        context.getResources().getString(R.string.action_buy))
                        .withIcon(GoogleMaterial.Icon.gmd_shop).withBadge(
                                context.getResources().getString(R.string.action_buy_description))
                        .withSelectable(false).withIdentifier(5),
                new SecondaryDrawerItem().withName(
                        context.getResources().getString(R.string.action_setting))
                        .withIcon(GoogleMaterial.Icon.gmd_settings).withSelectable(false).withIdentifier(6),
                new SecondaryDrawerItem().withName(
                        context.getResources().getString(R.string.action_about))
                        .withIcon(GoogleMaterial.Icon.gmd_info).withSelectable(false).withIdentifier(7));


        drawerBuilder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch ((int) drawerItem.getIdentifier()){
                    case 1:
                        recyclerView.setAdapter(appAdapter);
                        break;
                    case 2:
                        recyclerView.setAdapter(appSystemAdapter);
                        break;
                    case 3:
                        recyclerView.setAdapter(appFavoriteAdapter);
                        break;
                    case 4:
                        recyclerView.setAdapter(appHiddenAdapter);
                        break;
                    case 5:
                        break;
                    case 6:
                        context.startActivity(new Intent(context, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 7:
                        context.startActivity(new Intent(context,AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return drawerBuilder.build();
    }

    private static int getDayorNight() {
        int actualHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if(actualHour >= 8 && actualHour < 19){
            return 1;
        }else{
            return 0;
        }
    }
}
