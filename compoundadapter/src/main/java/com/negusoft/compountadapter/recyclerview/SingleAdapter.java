/*******************************************************************************
 * Copyright 2016 Negusoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.negusoft.compountadapter.recyclerview;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * RecyclerView.Adapter with one single configurable element.
 */
public class SingleAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    /** Delegate interfaces, composed of the creator and binder roles. */
    public interface Delegate<T extends RecyclerView.ViewHolder>
        extends  Creator<T>, Binder<T> {
    }
    public interface Creator<T extends RecyclerView.ViewHolder> {
        T createViewHolder(ViewGroup parent);
    }
    public interface Binder<T extends RecyclerView.ViewHolder> {
        void bindViewHolder(T viewHolder);
    }

    public static SingleAdapter create(final @LayoutRes View view) {
        return new SingleAdapter(new Creator<ViewHolder>() {
            @Override
            public ViewHolder createViewHolder(ViewGroup parent) {
                return new ViewHolder(view);
            }
        }, null);
    }

    public static SingleAdapter create(final @LayoutRes int layout) {
        return create(layout, null);
    }

    public static SingleAdapter create(final @LayoutRes int layout, final Binder<RecyclerView.ViewHolder> binder) {
        return new SingleAdapter<ViewHolder>(new Delegate<ViewHolder>() {
            @Override
            public ViewHolder createViewHolder(ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View view = inflater.inflate(layout, parent, false);
                return new ViewHolder(view);
            }
            @Override
            public void bindViewHolder(ViewHolder viewHolder) {
                if (binder != null) {
                    binder.bindViewHolder(viewHolder);
                }
            }
        });
    }

    // Delegate roles
    private final Creator<T> mCreator;
    private final Binder<T> mBinder;

    /**
     * New instance with a delegate that creates and binds the ViewHolder
     */
    public SingleAdapter(Delegate<T> delegate) {
        mCreator = delegate;
        mBinder = delegate;
    }

    /**
     * New instance with a creator and the binder to manage the ViewHolder
     */
    public SingleAdapter(Creator<T> creator, Binder<T> binder) {
        mCreator = creator;
        mBinder = binder;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return mCreator.createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        if (mBinder == null)
            return;
        mBinder.bindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
