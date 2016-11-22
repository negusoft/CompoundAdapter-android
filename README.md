 - Project: CompoundAdapter
 - Developer: Borja Lopez Urkidi
 - Organization: Negusoft
 - Web: http://www.negusoft.com


Description
===========

Android library that provides a way to define a RecyclerView.Adapter out of subadapters (AdapterGroup).


Features
========
- AdapterGroup: Adapter containing inner addapters.
- Nesting: AdapterGroups can have other AdapterGroups
- Performance: Child adapters of the same type can recycle ViewHolders (as expected)
- Edit the adapter structure while the AdapterGroup is attached to the RecyclerView


Basic Setup
===========

You can add CompoundAdapter to your project using the following gradle dependency:

or Gradle:
```groovy
compile 'com.negusoft.compoundadapter:compoundadapter:0.8.0'
```

You can then use the AdapterGroup class to combine RecyclerView.Adapters. For example the following code will display the content of two adapters in the same RecyclerView:

``` java
RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerview);

AdapterGroup adapterGroup = new AdapterGroup();
adapterGroup.addAdapter(new FirstAdapter());
adapterGroup.addAdapter(new SecondAdapter());

recyclerView.setAdapter(adapterGroup);
```


Relative Item Positions
=======================

Keep in mind that if you are using the ViewHolders position (getAdapterPosition(), getLayoutPosition()), the reported values are relative to the origin. That is, in the previous example, the onClick() implemented in the first item of the SecondAdapter will not report a position 0.


Adapter Types and Performance
=============================

A good thing about RecyclerViews is that they reuse ViewHolder to optimize performance. In order not to loose this optimizations, AdapterGroup groups RecyclerView.Adapters by type, such a way that they can reuse each others ViewHolders. This way, if you add two instances of MyBookAdapter, for example, the second instance might bind a ViewHolder that was instantiated by the first one.

By default, adapters are grouped by their java class. If you want more control on this, you may add them to the AdapterGroup by using addAdapter(adapter, type). This way, you can make adapters of different type share ViewHolders. Or the oposite, avoid that two adapters share ViewHolders.


Updating Data
==============

You can update the data with the usual methods: notifyDataSetChanged(), notifyItemInserted()... You can use this methods on the AdapterGroup directly or call the child Adapters.

Just make sure that the positions passed to the notifyItemXxx() method are relative to that specific adapter. That is, removing the first item in the Second adapter might be done like this:

``` java
// Remove the first item of secondAdapter
secondAdapter.notifyItemRangeRemoved(0);

// Same operation, given that firstAdapter has 5 items
adapterGroup.notifyItemRangeRemoved(5);
```


License
=======

    Copyright 2016 Negusoft

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
