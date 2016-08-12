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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An adapter made out of adapters.
 */
public class AdapterGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // A list with the adapter holder and a map for quick access by adapter
    private final List<AdapterHolder> mAdapterHolderList = new ArrayList<>(3);
    private final Map<RecyclerView.Adapter, AdapterHolder> mAdapterHolderMap = new HashMap<>(3);

    private final Map<String, AdaptersByType> mAdapterTypes = new HashMap<>();
    private final SparseArray<AdaptersByType> mAdapterTypesByViewType = new SparseArray<>();

    private int mTotalCount = 0;
    private boolean mRecyclerViewAttached = false;

    private ViewTypeGenerator mViewTypeGenerator = new ViewTypeGenerator(1);

    /**
     * Add the given adapter at the end.
     */
    public void addAdapter(RecyclerView.Adapter adapter) {
        addAdapter(mAdapterHolderList.size(), adapter, null);
    }

    /**
     * Add the given adapter.
     * @param location The index where the adapter will be inserted.
     */
    public void addAdapter(int location, RecyclerView.Adapter adapter) {
        addAdapter(location, adapter, null);
    }

    /**
     * Add the given adapter at the end.
     * @param adapterType Adapters of the same type reuse each others ViewHolders. By default,
     *                    adapters are grouped by class.
     */
    public void addAdapter(RecyclerView.Adapter adapter, @Nullable String adapterType) {
        addAdapter(mAdapterHolderList.size(), adapter, adapterType);
    }

    /**
     * Add the given adapter.
     * @param location The index where the adapter will be inserted.
     * @param adapterType Adapters of the same type reuse each others ViewHolders. By default,
     *                    adapters are grouped by class.
     */
    public void addAdapter(int location, RecyclerView.Adapter adapter, @Nullable String adapterType) {
        if (adapterType == null)
            adapterType = adapter.getClass().toString();

        AdapterHolder holder = new AdapterHolder(adapter, adapterType);
        if (mAdapterHolderMap.containsKey(adapter))
            throw new InvalidParameterException("The adapter is already present in the CompoundAdapter");

        mAdapterHolderList.add(location, holder);
        mAdapterHolderMap.put(adapter, holder);

        // Set the parent reference if the adapter is a AdapterGroup
        if (adapter instanceof AdapterGroup) {
            ((AdapterGroup)adapter).mRecyclerViewAttached = mRecyclerViewAttached;
        }

        // Register the data observer if we are already attached to the RecyclerView
        if (mRecyclerViewAttached) {
            updateIndexing();
            holder.registerDataObserver();

            notifyItemRangeInserted(holder.startPosition, holder.count);
        }
    }

    /**
     * Remove the given adapter.
     */
    public void removeAdapter(RecyclerView.Adapter adapter) {
        AdapterHolder removedHolder = mAdapterHolderMap.remove(adapter);
        if (removedHolder == null)
            return;

        mAdapterHolderList.remove(removedHolder);

        if (mRecyclerViewAttached) {
            removedHolder.unregisterDataObserver();
            notifyItemRangeRemoved(removedHolder.startPosition, removedHolder.count);
        }
    }

    /**
     * Returns the adapter at the given index.
     */
    public RecyclerView.Adapter getAdapter(int location) {
        return mAdapterHolderList.get(location).adapter;
    }

    /**
     * Returns a list of all the adapters that compose the AdapterGroup.
     */
    public List<RecyclerView.Adapter> getAdapters() {
        List<RecyclerView.Adapter> result = new ArrayList<>();
        for (AdapterHolder holder : mAdapterHolderList) {
            result.add(holder.adapter);
        }
        return result;
    }

    /**
     * @return True if the adapter is part of this adapter group. False otherwise.
     */
    public boolean containsAdapter(RecyclerView.Adapter adapter) {
        return mAdapterHolderMap.containsKey(adapter);
    }

    /**
     * @return The index of the given adapter or -1 if it was not found.
     */
    public int indexOfAdapter(RecyclerView.Adapter adapter) {
        AdapterHolder holder = mAdapterHolderMap.get(adapter);
        if (holder == null)
            return -1;

        return mAdapterHolderList.indexOf(holder);
    }

    /**
     * Returns the adapter at the given position along with the mapped position
     */
    public AdapterPosition getAdapterAtItemPosition(int position) {
        updateIndexing();

        AdapterHolder holder = getAdapterHolderForIndex(position);
        int index = holder.mapPosition(position);

        return new AdapterPosition(holder.adapter, index);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdaptersByType adaptersByType = mAdapterTypesByViewType.get(viewType);
        int innerViewType = adaptersByType.getInnerViewType(viewType);
        RecyclerView.Adapter adapter = adaptersByType.adapters.iterator().next();

        return adapter.onCreateViewHolder(parent, innerViewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AdapterHolder adapterHolder = getAdapterHolderForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        adapterHolder.adapter.onBindViewHolder(holder, innerPosition);
    }

    @Override
    public int getItemCount() {
        // TODO update index only if required
        updateIndexing();
        return mTotalCount;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(position, mAdapterTypes, mAdapterTypesByViewType, mViewTypeGenerator);
    }

    private int getItemViewType(int position, Map<String, AdaptersByType> adapterTypes, SparseArray<AdaptersByType> adapterTypesByViewType, ViewTypeGenerator viewTypeGenerator) {
        AdapterHolder adapterHolder = getAdapterHolderForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);

        // IF the adapter is AdapterGroup -> dive into the sub-adapters
        if (adapterHolder.adapter instanceof AdapterGroup) {
            return ((AdapterGroup)adapterHolder.adapter).getItemViewType(innerPosition, adapterTypes, adapterTypesByViewType, viewTypeGenerator);
        }

        // Add the view type mapping in the corresponding adapter type group
        int innerViewType = adapterHolder.adapter.getItemViewType(innerPosition);
        AdaptersByType adapterGroupsByType = adapterTypes.get(adapterHolder.adapterType);
        if (adapterGroupsByType == null) {
            adapterGroupsByType = new AdaptersByType(viewTypeGenerator);
            adapterTypes.put(adapterHolder.adapterType, adapterGroupsByType);
        }
        if (!adapterGroupsByType.adapters.contains(adapterHolder.adapter)) {
            adapterGroupsByType.adapters.add(adapterHolder.adapter);
        }

        int outerViewType = adapterGroupsByType.getOuterViewType(innerViewType);
        adapterTypesByViewType.put(outerViewType, adapterGroupsByType);

        return outerViewType;
    }

    @Override
    public long getItemId(int position) {
        AdapterHolder adapterHolder = getAdapterHolderForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        return adapterHolder.adapter.getItemId(innerPosition);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        setRecyclerViewAttached(this, true);
        registerAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        setRecyclerViewAttached(this, false);
        unregisterAdapterDataObserver(mAdapterDataObserver);
    }

    /** Recursive method to set the attached flags through the AdapterGroup hierarchy. */
    private void setRecyclerViewAttached(AdapterGroup adapterGroup, boolean attached) {
        adapterGroup.mRecyclerViewAttached = attached;
        for (AdapterHolder holder : adapterGroup.mAdapterHolderList) {
            // Register/Unregister observers
            if (attached) {
                holder.registerDataObserver();
            } else {
                holder.unregisterDataObserver();
            }

            // Dive deeper if for each child AdapterGroup
            if (holder.adapter instanceof AdapterGroup) {
                setRecyclerViewAttached((AdapterGroup)holder.adapter, attached);
            }
        }
    }

    private void updateIndexing() {
        int counter = 0;
        for (AdapterHolder holder : mAdapterHolderList) {
            counter += holder.updateIndex(counter);
        }
        mTotalCount = counter;
    }

    private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            updateIndexing();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            updateIndexing();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            updateIndexing();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            updateIndexing();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            updateIndexing();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            updateIndexing();
        }
    };

    private AdapterHolder getAdapterHolderForIndex(int index) {
        for (AdapterHolder holder : mAdapterHolderList) {
            int mapped = holder.mapPosition(index);
            if (mapped >= 0 && mapped < holder.count)
                return holder;
        }
        throw new IndexOutOfBoundsException("Failed to map the index to an inner adapter");
    }

    /** Represents an inner Adapter along info related to it. */
    class AdapterHolder {
        final RecyclerView.Adapter adapter;
        final String adapterType;

        AdapterHolderDataObserver dataObserver;
        int startPosition = -1;
        int count = -1;

        AdapterHolder(RecyclerView.Adapter adapter, String adapterType) {
            this.adapter = adapter;
            this.adapterType = adapterType;
        }

        int updateIndex(int position) {
            startPosition = position;
            count = adapter.getItemCount();
            return count;
        }

        /** Map position: AdapterGroup -> child adapter */
        int mapPosition(int position) {
            return position - startPosition;
        }

        /** Map position: child adapter -> AdapterGroup */
        int mapPositionInverse(int position) {
            return position + startPosition;
        }

        void registerDataObserver() {
            if (dataObserver != null)
                return;
            dataObserver = new AdapterHolderDataObserver(this);
            adapter.registerAdapterDataObserver(dataObserver);

            // Register child adapters data observers
            if (adapter instanceof AdapterGroup) {
                for (AdapterHolder holder : ((AdapterGroup)adapter).mAdapterHolderList) {
                    holder.registerDataObserver();
                }
            }
        }

        void unregisterDataObserver() {
            if (dataObserver == null)
                return;
            adapter.unregisterAdapterDataObserver(dataObserver);
            dataObserver = null;

            // Unregister child adapters data observers
            if (adapter instanceof AdapterGroup) {
                for (AdapterHolder holder : ((AdapterGroup)adapter).mAdapterHolderList) {
                    holder.unregisterDataObserver();
                }
            }
        }
    }

    /** Maps from outer to inner view types and vice versa. */
    class ViewTypeMapping {
        private SparseIntArray in2outMapping = new SparseIntArray();
        private SparseIntArray out2inMapping = new SparseIntArray();
    }

    /** A set of AdapterGroups of the same type, which share the view type mapping. */
    class AdaptersByType {
        // Use weak references for the set of adapters.
        final Set<RecyclerView.Adapter> adapters = Collections.newSetFromMap(new WeakHashMap<RecyclerView.Adapter, Boolean>(2));;
        final ViewTypeMapping viewTypeMapping = new ViewTypeMapping();
        final ViewTypeGenerator viewTypeGenerator;

        AdaptersByType(ViewTypeGenerator viewTypeGenerator) {
            this.viewTypeGenerator = viewTypeGenerator;
        }

        int getOuterViewType(int innerViewType) {
            int outerViewType = viewTypeMapping.in2outMapping.get(innerViewType, 0);
            if (outerViewType != 0)
                return outerViewType;

            // The view type is not mapped -> generate a outer view type
            outerViewType = viewTypeGenerator.getNext();
            viewTypeMapping.in2outMapping.put(innerViewType, outerViewType);
            viewTypeMapping.out2inMapping.put(outerViewType, innerViewType);

            return outerViewType;
        }

        int getInnerViewType(int outerViewType) {
            return viewTypeMapping.out2inMapping.get(outerViewType);
        }
    }

    /** AdapterDataObserver for each of the Adapters in order to forward changes to the parent. */
    class AdapterHolderDataObserver extends RecyclerView.AdapterDataObserver {

        final AdapterHolder holder;

        AdapterHolderDataObserver(AdapterHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onChanged() {
            updateIndexing();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeInserted(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeRemoved(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(fromPosition);
            notifyItemRangeRemoved(innerPositionStart, itemCount);
        }
    }

    private class ViewTypeGenerator {
        private int nextViewType;

        ViewTypeGenerator(int startAt) {
            nextViewType = startAt;
        }

        /** Returns a different incremental integer value each time it is called. */
        private int getNext() {
            return nextViewType++;
        }
    }
}