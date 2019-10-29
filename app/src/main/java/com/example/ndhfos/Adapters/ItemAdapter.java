package com.example.ndhfos.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;
import com.example.ndhfos.R;

import java.util.List;
import java.util.Locale;

public class ItemAdapter extends ArrayAdapter<Item> {

    private ItemsDatabase database;
    private Menu menu;
    private TextView cartItemCountTV;

    private static final String LOG_TAG = ItemAdapter.class.getSimpleName();

    public ItemAdapter(Context context, List<Item> objects, Menu menu) {
        super(context, 0, objects);
        this.menu = menu;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_item_item,parent,false);

        TextView itemNameTV = convertView.findViewById(R.id.item_name_tv);
        TextView priceTV = convertView.findViewById(R.id.price_tv);
        TextView quantityTV = convertView.findViewById(R.id.current_quantity_tv);

        ImageView itemImage = convertView.findViewById(R.id.item_image);

        Button addToCartBT = convertView.findViewById(R.id.add_to_cart_bt);
        Button increaseQuantityBT = convertView.findViewById(R.id.increase_quantity);
        Button decreaseQuantityBT = convertView.findViewById(R.id.decrease_quantity);

        LinearLayout quantityChanger = convertView.findViewById(R.id.quantity_changer);

        MenuItem cartItem = menu.findItem(R.id.add_to_cart);

        View actionView = cartItem.getActionView();
        cartItemCountTV = actionView.findViewById(R.id.cart_badge);

        Item item = getItem(position);
        database = ItemsDatabase.getInstance(getContext());

        if(item==null)
            return super.getView(position, convertView, parent);

        List<Item> cart = database.itemDAO().viewItems();

        addToCartBT.setOnClickListener((event)->{
            addToCart(item);
            addToCartBT.setVisibility(View.GONE);
            quantityChanger.setVisibility(View.VISIBLE);
            quantityTV.setText(String.valueOf(1));
        });

        increaseQuantityBT.setOnClickListener((event)->{
            int currentQuantity = updateCart(item,true);
            quantityTV.setText(String.valueOf(currentQuantity));
        });

        decreaseQuantityBT.setOnClickListener((event)->{
            int currentQuantity = updateCart(item,false);
            if(currentQuantity == 0){
                quantityChanger.setVisibility(View.GONE);
                addToCartBT.setVisibility(View.VISIBLE);
            } else
                quantityTV.setText(String.valueOf(item.getQuantity()));
            quantityTV.setText(String.valueOf(item.getQuantity()));
        });

        itemNameTV.setText(item.getName());
        priceTV.setText(
                String.format(
                        Locale.getDefault(),
                        "₹ %.2f",(float)item.getPrice()
                )
        );
        if(item.getImage()!= null)
            itemImage.setImageURI(item.getImage());
        convertView.setTag(item.getKey());

        if(!cart.isEmpty()){

            addToCartBT.setVisibility(View.VISIBLE);
            quantityChanger.setVisibility(View.GONE);
            cartItemCountTV.setVisibility(View.VISIBLE);

            cartItemCountTV.setText(String.valueOf(cart.size()));

            for(Item currentItem : cart){

                if(currentItem.getKey().equals(item.getKey())){

                    addToCartBT.setVisibility(View.GONE);
                    quantityChanger.setVisibility(View.VISIBLE);
                    quantityTV.setText(String.valueOf(currentItem.getQuantity()));

                }

            }

        } else {

            addToCartBT.setVisibility(View.VISIBLE);
            quantityChanger.setVisibility(View.GONE);
            cartItemCountTV.setVisibility(View.GONE);

        }

        return convertView;

    }

    private void addToCart(Item item){

        Log.i(LOG_TAG,"Added "+item.getName()+" to cart.");
        item.setQuantity(1);
        database.itemDAO().insertItem(item);
        int currentItems = Integer.parseInt(cartItemCountTV.getText().toString());
        cartItemCountTV.setVisibility(View.VISIBLE);
        cartItemCountTV.setText(String.valueOf(currentItems+1));
    }

    private int updateCart(Item item, boolean increase){


        int currentQuantity = item.getQuantity()+(increase?1:-1);
        Log.i(LOG_TAG, currentQuantity+" "+item.getName()+"s in cart");
        if(currentQuantity <= 0) {
            database.itemDAO().deleteItem(item);
            currentQuantity = 0;
        } else {
            item.setQuantity(currentQuantity);
            database.itemDAO().updateItem(item);
        }

        List<Item> items = database.itemDAO().viewItems();

        int itemsCount = items.size();

        if(itemsCount == 0) {
            cartItemCountTV.setVisibility(View.GONE);
            cartItemCountTV.setText(String.valueOf(0));
        } else {

            cartItemCountTV.setText(String.valueOf(Math.min(itemsCount, 99)));
            cartItemCountTV.setVisibility(View.VISIBLE);

        }

        return currentQuantity;

    }

}