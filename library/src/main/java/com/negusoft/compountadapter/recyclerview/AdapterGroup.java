package com.negusoft.compountadapter.recyclerview;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An adapter made out of adapters.
 */
public class AdapterGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LinkedHashMap<RecyclerView.Adapter, AdapterHolder> mAdapterHolders = new LinkedHashMap<>();

    private Map<String, AdaptersByType> mAdapterTypes = new HashMap<>();
    private SparseArray<AdaptersByType> mAdapterTypesByViewType = new SparseArray<>();

    private boolean mIndexingRequired = true;
    private int mTotalCount = 0;
    private boolean mRecyclerViewAttached = false;

    private ViewTypeGenerator mViewTypeGenerator = new ViewTypeGenerator(1);

    private AdapterGroup mParent;

    /**
     * Add the given adapter.
     */
    public void addAdapter(RecyclerView.Adapter adapter) {
        addAdapter(adapter, null);
    }

    /**
     * Add the given adapter.
     * @param adapterType Adapters of the same type reuse each others ViewHolders. By default,
     *                    adapters are grouped by class.
     */
    public void addAdapter(RecyclerView.Adapter adapter, @Nullable String adapterType) {
        if (adapterType == null)
            adapterType = adapter.getClass().toString();

        AdapterHolder holder = new AdapterHolder(adapter, adapterType);
        if (mAdapterHolders.containsKey(adapter))
            throw new InvalidParameterException("The adapter is already present in the CompoundAdapter");
        mAdapterHolders.put(adapter, holder);

        // Set the parent reference if the adapter is a AdapterGroup
        if (adapter instanceof AdapterGroup) {
            ((AdapterGroup)adapter).mParent = this;
        }

        // Register the data observer if we are already attached to the RecyclerView
        if (mRecyclerViewAttached) {
            holder.registerDataObserver();
        }

        // Update data
        mIndexingRequired = true;
//        notifyDataSetChanged();
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
//        notifyDataSetChanged();
    }

    /**
     * @return True if the adapter is part of this adapter group. False otherwise.
     */
    public boolean containsAdapter(RecyclerView.Adapter adapter) {
        return mAdapterHolders.containsKey(adapter);
    }

    /**
     * Returns false if this adapter is held withing another AdapterGroup. true otherwise.
     */
    public boolean isRootAdapter() {
        return mParent == null;
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

    /**
     * Find the root of the adapter hierarchy. It might be this instance.
     */
    public AdapterGroup getRootAdapter() {
        AdapterGroup currentAdapterGroup = this;
        while (currentAdapterGroup.mParent != null) {
            currentAdapterGroup = currentAdapterGroup.mParent;
        }

        return currentAdapterGroup;
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
        return getItemViewType(position, mAdapterTypes, mAdapterTypesByViewType, mViewTypeGenerator);
    }

    private int getItemViewType(int position, Map<String, AdaptersByType> adapterTypes, SparseArray<AdaptersByType> adapterTypesByViewType, ViewTypeGenerator viewTypeGenerator) {
        AdapterHolder adapterHolder = getAdapterForIndex(position);
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
        AdapterHolder adapterHolder = getAdapterForIndex(position);
        int innerPosition = adapterHolder.mapPosition(position);
        return adapterHolder.adapter.getItemId(innerPosition);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
//        mRecyclerViewAttached = true;
        setRecyclerViewAttached(this, true);
        registerAdapterDataObserver(mAdapterDataObserver);
//        for (AdapterHolder holder : mAdapterHolders.values()) {
//            holder.registerDataObserver();
//        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
//        mRecyclerViewAttached = false;
        setRecyclerViewAttached(this, false);
        unregisterAdapterDataObserver(mAdapterDataObserver);
//        for (AdapterHolder holder : mAdapterHolders.values()) {
//            holder.unregisterDataObserver();
//        }
    }

    /** Recursive method to set the attached flags through the AdapterGroup hierarchy. */
    private void setRecyclerViewAttached(AdapterGroup adapterGroup, boolean attached) {
        adapterGroup.mRecyclerViewAttached = attached;
        for (AdapterHolder holder : adapterGroup.mAdapterHolders.values()) {
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
            updateIndexing();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mIndexingRequired = true;
            updateIndexing();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
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
                for (AdapterHolder holder : ((AdapterGroup)adapter).mAdapterHolders.values()) {
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
                for (AdapterHolder holder : ((AdapterGroup)adapter).mAdapterHolders.values()) {
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
            mIndexingRequired = true;
            updateIndexing();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mIndexingRequired = true;
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeChanged(innerPositionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeInserted(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mIndexingRequired = true;
            updateIndexing();
            int innerPositionStart = holder.mapPositionInverse(positionStart);
            notifyItemRangeRemoved(innerPositionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mIndexingRequired = true;
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