/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.model.Song;

public class SwipeAndDragHelper extends ItemTouchHelper.Callback {

  private final ActionCompletionContract contract;
  private final Context context;

  public SwipeAndDragHelper(@NonNull ActionCompletionContract contract, @NonNull Context context) {
    this.contract = contract;
    this.context = context;
  }

  @Override
  public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
      int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
      int swipeFlags = 0;
      if (PreferenceUtil.INSTANCE.isSwipeToQueueEnabled() &&
              contract.canSwipeItem(viewHolder.getLayoutPosition())) {
          swipeFlags = ItemTouchHelper.RIGHT;
      }

      return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(
          @NonNull RecyclerView recyclerView,
          RecyclerView.ViewHolder viewHolder,
          RecyclerView.ViewHolder target) {
    contract.onViewMoved(viewHolder.getLayoutPosition(), target.getLayoutPosition());
    return true;
  }

  @Override
  public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    contract.onViewSwiped(viewHolder.getLayoutPosition());
  }

  public void addSongToQueue(@NonNull Song song) {
    MusicPlayerRemote.INSTANCE.playNext(song);
    String message = context.getString(R.string.added_title_to_playing_queue, song.getTitle());
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean isLongPressDragEnabled() {
    return false;
  }

  @Override
  public void onChildDraw(
          @NonNull Canvas c,
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder,
          float dX,
          float dY,
          int actionState,
          boolean isCurrentlyActive) {
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
      if (dX > 0) {
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.widget_circle_button_color));

        c.drawRect((float) itemView.getLeft(),
                   (float) itemView.getTop(),
                   (float) itemView.getLeft() + dX,
                   (float) itemView.getBottom(), paint);

        Drawable icon = context.getDrawable(R.drawable.ic_queue_music);
        if (icon != null) {
          int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
          int top = itemView.getTop() + margin;
          int left = itemView.getLeft() + margin;
          int right = left + icon.getIntrinsicWidth();
          int bottom = top + icon.getIntrinsicHeight();
          icon.setBounds(left, top, right, bottom);
          icon.draw(c);
        }
      }

      float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
      viewHolder.itemView.setAlpha(alpha);
      viewHolder.itemView.setTranslationX(dX);
    } else {
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
  }

  public interface ActionCompletionContract {
    void onViewMoved(int oldPosition, int newPosition);

    void onViewSwiped(int position);

    boolean canSwipeItem(int position);
  }
}
