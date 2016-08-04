package com.negusoft.compountadapter.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An adapter made out of adapters.
 */
public class AdapterGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LinkedHashMap<RecyclerView.Adapter, AdapterHolder> mAdapterHolders = new LinkedHashMap<>();

    // Links the view type ids with the corresponding adapter holder.
    private SparseArray<AdapterHolder> mViewTypeMapping = new SparseArray<>();

    private boolean mIndexingRequired = true;
    private int mTotalCount = 0;
    private boolean mRecyclerViewAttached = false;

    private ViewTypeGenerator mViewTypeGenerator = new ViewTypeGenerator(1);

    /**
     * Add the given adapter.
     */
    public void addAdapter(RecyclerView.Adapter adapter) {
        AdapterHolder holder = new AdapterHolder(adapter);
        if (mAdapterHolders.containsKey(adapter))
            throw new InvalidParameterException("The adapter is already present in the CompoundAdapter");
        mAdapterHolders.put(adapter, holder);

        // Register the data observer if we are already attached to the RecyclerView
        if (mRecyclerViewAttached) {
            holder.registerDataObserver();
        }

        // Update data
        mIndexingRequired = true;
        notifyDataSetChanged();
    }

    /**
     * @return True if the adapter is part of this adapter group. False otherwise.
     */
    public boolean containsAdapter(RecyclerView.Adapter adapter) {
        return mAdapterHolders.containsKey(adapter);
    }

    /**
     * Remove the given adapter.
     */
    public void removeAdapter(RecyclerView.Adapter adapter) {
        AdapterHolder removedHolder = mAdapterHolders.remove(adapter);
        if (removedHolder == null)
            return;

        // Clean up
        removedHolder.unregisterDataObserver();

        // Update data
        mIndexingRequired = true;
        notifyDataSetChanged();
    }

    /**
     * Returns the adapter at the given position along with the mapped position
     */
    public AdapterPosition getAdapterAtPosition(int position) {
        updateIndexing();

        AdapterHolder holder = getAdapterForIndex(position);
        int index = holder.mapPosition(position);

        return new AdapterPosition(holder.adapter, index);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdapterHolder adapterHolder = mViewTypeMapping.get(viewType);
        int innerViewType = adapterHolder.getInnerViewType(viewType);
        return adapterHolder.adapter.onCreateViewHolder(parent, innerViewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AdapterHolder adapterHolder = getAdapterForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        adapterHolder.adapter.onBindViewHolder(holder, innerPosition);
    }

    @Override
    public int getItemCount() {
        updateIndexing();
        return mTotalCount;
    }

    @Override
    public int getItemViewType(int position) {
        AdapterHolder adapterHolder = getAdapterForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        int innerViewType = adapterHolder.adapter.getItemViewType(innerPosition);

        return adapterHolder.getOuterViewType(innerViewType);
    }

    @Override
    public long getItemId(int position) {
        AdapterHolder adapterHolder = getAdapterForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        return adapterHolder.adapter.getItemId(innerPosition);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViewAttached = true;
        registerAdapterDataObserver(mAdapterDataObserver);
        for (AdapterHolder holder : mAdapterHolders.values()) {
            holder.registerDataObserver();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerViewAttached = false;
        unregisterAdapterDataObserver(mAdapterDataObserver);
        for (AdapterHolder holder : mAdapterHolders.values()) {
            holder.unregisterDataObserver();
        }
    }

    private void updateIndexing() {
        if (!mIndexingRequired)
            return;

        mIndexingRequired = false;
        int counter = 0;
        for (Map.Entry<RecyclerView.Adapter, AdapterHolder> entry : mAdapterHolders.entrySet()) {
            counter += entry.getValue().updateIndex(counter);
        }
        mTotalCount = counter;
    }

    private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mIndexingRequired = true;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mIndexingRequired = true;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mIndexingRequired = true;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mIndexingRequired = true;
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mIndexingRequired = true;
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mIndexingRequired = true;
        }
    };

    private AdapterHolder getAdapterForIndex(int index) {
        for (AdapterHolder holder : mAdapterHolders.values()) {
            int mapped = holder.mapPosition(index);
            if (mapped >= 0 && mapped < holder.count)
                return holder;
        }
        throw new IndexOutOfBoundsException("Failed to map the index to an inner adapter");
    }

    /** Represents an inner Adapter along info related to it. */
    class AdapterHolder {
        final RecyclerView.Adapter adapter;

        AdapterHolderDataObserver dataObserver;
        int startPosition = -1;
        int count = -1;

        // Mappings from outer to inner view types and vice versa.
        SparseIntArray in2outMapping = new SparseIntArray();
        SparseIntArray out2inMapping = new SparseIntArray();

        AdapterHolder(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        int updateIndex(int position) {
            startPosition = position;
            // TODO don't count if the adapter didn't change
            count = adapter.getItemCount();
            return count;
        }

        int mapPosition(int position) {
            return position - startPosition;
        }

        void registerDataObserver() {
            if (dataObserver != null)
                return;
            dataObserver = new AdapterHolderDataObserver(this);
            adapter.registerAdapterDataObserver(dataObserver);
        }

        void unregisterDataObserver() {
            if (dataObserver == null)
                return;
            adapter.unregisterAdapterDataObserver(dataObserver);
            dataObserver = null;
        }

        int getOuterViewType(int innerViewType) {
            int outerViewType = in2outMapping.get(innerViewType, 0);
            if (outerViewType != 0)
                return outerViewType;

            // The view type is not mapped -> generate a outer view type
            outerViewType = mViewTypeGenerator.getNext();
            in2outMapping.put(innerViewType, outerViewType);
            out2inMapping.put(outerViewType, innerViewType);

            // Link the generated view type to this adapter holder
            mViewTypeMapping.put(outerViewType, this);

            return outerViewType;
        }

        int getInnerViewType(int outerViewType) {
            return out2inMapping.get(outerViewType);
        }
    }

    /** Maps from outer to inner view types and vice versa. **/
    class ViewTypeMapping {
        private SparseIntArray in2outMapping = new SparseIntArray();
        private SparseIntArray out2inMapping = new SparseIntArray();
    }

    /** AdapterDataObserver for each of the Adapters in order to forward changes to the parent. */
    class AdapterHolderDataObserver extends RecyclerView.AdapterDataObserver {

        final AdapterHolder holder;

        AdapterHolderDataObserver(AdapterHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            int innerPositionStart = holder.mapPosition(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            int innerPositionStart = holder.mapPosition(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            int innerPositionStart = holder.mapPosition(positionStart);
            notifyItemRangeInserted(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            int innerPositionStart = holder.mapPosition(positionStart);
            notifyItemRangeRemoved(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            // Find a more appropriate notification method ???
            notifyDataSetChanged();
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