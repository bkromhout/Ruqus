# Ruqus - Realm User Query System 🔎

Ruqus is an Android library which provides a number of components that allow you to let your users construct their own queries against your [Realm][Realm Java] data set. These are stored in `RealmUserQuery` objects, which can then be turned into `RealmResults` using a single method call.

Ruqus makes use of an annotation processor to generate information about your model object classes at compile-time, thus allowing your app to avoid reflection as much as possible at run-time.

#### Table of Contents
* [Installation](#installation)  
* [Basic Setup](#basics)  
    * [Initializing Ruqus](#init)
* [Model Annotations](#annotations)  
    * [`@Queryable`](#queryable)
    * [`@Hide`](#hide)
    * [`@VisibleAs`](#visible_as)
* [Transformers](#transformers)
* [Troubleshooting](#troubleshooting)

<a name="screenshots"/>
#### Screenshots:
Initial View ![Initial View](Screenshots/Results_Empty.png) Initial RealmQueryView ![Initial RealmQueryView](Screenshots/Edit_Empty.png) Choosing Query Type ![Choosing Query Type](Screenshots/Edit_Choose_Type.png)
Editing a Condition 1 ![Editing a Condition 1](Screenshots/Edit_Cond_Age.png) Editing a Condition 2 ![Editing a Condition 2](Screenshots/Edit_Cond_Age_2.png) Adding an Operator ![Adding an Operator](Screenshots/Edit_Add_Op.png)
Editing Sort Fields ![Editing Sort Fields](Screenshots/Edit_Sort.png) Filled RealmQueryView ![Filled RealmQueryView](Screenshots/Edit_Filled.png) Results ![Results](Screenshots/Results.png)

<a name="installation"/>
## Installation
Including Ruqus in your app is pretty simple, just make sure that you have the following in your root `build.gradle` file:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

And then add this to your app's `build.gradle` file:
```groovy
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    apt 'com.bkromhout.ruqus:ruqus-compiler:{latest version}'
    compile ('com.bkromhout.ruqus:ruqus-core:{latest version}@aar') {
        transitive = true
    }
}
```
Please note that at this time, Ruqus has been tested and is verified to work with **Realm 0.88.2**. Don't be afraid to try a newer version of Realm, just be sure to open an issue if you run into problems.

<a name="basics"/>
## The Basics
Ruqus relies on a number of annotations to help it generate information at compile-time.  
At a high level, the processor uses Realm's `@RealmClass` annotation to figure out which classes in your project are `RealmObject`s. Since `RealmObject` is annotated with `@RealmClass`, all of your model objects will automatically be picked up by the annotation processor.

Ruqus also provides a few annotations which you should use to help the processor generate extra information about your model object classes. These are discussed [a little further down](#annotations).

Before that, however, there are a few things which must be done to make Ruqus work.

<a name="init"/>
### Initializing Ruqus
You **must** call `Ruqus.init(Context)` sometime before you handle `RealmUserQuery` objects or the user has the ability to interact with `RealmQueryView`s.  
I personally feel that the best place for this call to be made is in the `onCreate()` method of a custom Application class, as can be seen in the sample app's [`SampleApplication`][SampleApplication Class] class:
```java
public class SampleApplication extends Application {
    ...
    @Override
    public void onCreate() {
        super.onCreate();
        ...
        Ruqus.init(this);
        ...
    }
    ...
}
```

### Adding a `RealmQueryView`
For users to build a query, they obviously need some sort of UI control to interact with; Ruqus provides such a thing in the form of the `RealmQueryView`. Before we get into how to add one to a layout and hook it up in code, there are a couple of things you should know:

* A `RealmQueryView` is a fairly space-hungry control; for the best user experience, I recommend having nothing else on the screen at the same time (for phones), or at the very least ensuring that there are no other views above or below it (tablets)
* `RealmQueryView` will save/restore its view state when configuration changes occur all by itself (unless you're doing something funky and interrupting Android's `onSaveInstanceState()`/`onRestoreInstanceState()` call hierarchy)

Keeping that in mind, I'd recommend creating a new empty activity to use as a query building activity and adding a `RealmQueryView` to it. You can see the sample app's [activity_edit_query.xml][EditQueryActivity Layout] file if you want, but this is what it boils down to:
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

    <com.bkromhout.ruqus.RealmQueryView
            android:id="@+id/rqv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
</RelativeLayout>
```

Once you've done this, I highly recommend that you have your corresponding activity implement `RealmQueryView.ModeListener` so that it is notified when `RealmQueryView`'s mode changes. Again, you can look at the sample app's [`EditQueryActivity.java`][EditQueryActivity Class] file if you'd like, but here's what it boils down to:
```java
class EditQueryActivity implements RealmQueryView.ModeListener {
    RealmQueryView rqv;
    // Have this set to MAIN by default.
    RealmQueryView.Mode rqvMode = RealmQueryView.Mode.MAIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        // Bind the RealmQueryView.
        rqv = (RealmQueryView) findViewById(R.id.rqv);

        // Ensure your save button is visible (or if it's a toolbar icon,
        // do it in onPrepareOptionsMenu() instead).
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register this activity with the view to be notified of mode changes.
        rqv.setModeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Make sure the view doesn't hold onto a reference to the activity!
        rqv.clearModeListener();
    }

    @Override
    public void rqvModeChanged(RealmQueryView.Mode newMode) {
        rqvMode = newMode;

        // Do something to show your save trigger view if newMode is MAIN,
        // or to hide it if not.
    }
}
```
The reason for this is that `RealmQueryView` can be in a main mode, or a builder mode; it logically makes sense have a button/toolbar action/etc visible to let the user save their query while in main mode, but not while in a builder mode. See [the screenshots at the top](#screenshots) and notice how the sample app shows/hides the check button in the toolbar based on the state of the `RealmQueryView`.

While I won't discuss these in detail here since the sample code is fairly straight-forward, there are a couple key functionalities I recommend implementing:
* Structure your app so that your query builder activity is started using `startActivityForResult()`; then, when the query is saved, have it return a `RealmUserQuery` by putting it in the extras of an `Intent`.
* Similarly, if you want users to be able to edit an existing `RealmUserQuery`s, you can put it into the extras of the `Intent` used to *start* the query builder activity, and then pass it to the `RealmQueryView` using its `setRealmUserQuery()` method sometime during `onCreate()`.

Both of these are possible because `RealmUserQuery` implements Android's `Parcelable` interface.

<a name="annotations"/>
## Model Annotations

Ruqus includes a few annotations which you apply to your model objects to help it:

* Generate class/field data at compile-time
* Allow users to build queries against only the model objects which you include using only the fields which you don't exclude
* Specify "nice", human-readable names for model objects and fields

<a name="queryable"/>
### `@Queryable`
You annotate your model object classes with `@Queryable` if you want your users to be able to construct `RealmUserQuery`s which can produce `RealmResults` of that type.  
That is, if a model's class isn't annotated with `@Queryable`, users won't be able to build queries which return objects of that type.

The `@Queryable` annotation takes one **required** parameter called `name`, which you should consider to be a human-readable name for your model object that users will see.

Here's an example which uses the [`Person`][Person Class] class from the sample app:
```java
@Queryable(name = "Person")
public class Person extends RealmObject {
    ...
    private Dog dog;
    ...
}
```
This will allow users to build queries which return `Person` objects.

But notice that there's a `Dog`-typed field in `Person`. If we look at the [`Dog`][Dog Class] class from the sample app, notice that it *does not* have the `@Queryable` annotation:
```java
public class Dog extends RealmObject {
    ...
}
```
However, Ruqus will still generate information for the `Dog` class in order to support Realm's link queries.  
So in our sample app, users cannot build a query which returns `Dog` objects; but since our queryable class `Person` references the `Dog` class, they can build queries which return `Person` objects based on the linked `Dog` objects' fields. If this is still confusing, I'd recommend reading up on [Realm's link query functionality][Realm Link Queries].

<a name="hide"/>
### `@Hide`
It's fairly common for your model objects to have fields which you don't want users to be able to use when constructing queries. Ruqus will automatically skip fields which are annotated with [Realm's `@Ignore` annotation][Realm Ignore], but for cases where you *do* want Realm to have a field, but you *don't* want users to be able to use it, annotate that field with `@Hide`.

Here's an example which uses the [`Person`][Person Class] class from the sample app:
```java
@Queryable(name = "Person")
public class Person extends RealmObject {
    ...
    @Ignore
    private int tempReference;
    @Hide
    private long id;
    ...
}
```
From the Ruqus annotation processor's point of view, the meaning of both `@Ignore` and `@Hide` are the same: Skip the field. So, users won't be able to use the `tempReference` or the `id` fields when building `Person` queries.

While `@Hide` may seem quite trivial in theory, thoughtful use of it is essential for guarding against [OOM errors causing your app to crash](#ts_oom).

<a name="visible_as"/>
### `@VisibleAs`

<a name="transformers"/>
## Transformers

<a name="troubleshooting"/>
## Troubleshooting

<a name="ts_oom"/>
##### My app is crashing due to OOM errors!
This is, sadly, a fairly easy thing to cause depending on what relationships you have set up between your various model classes.  
The root of the problem is that you have a cycle of relationships, whether it be something like `A-->A`, `A-->B-->A`, etc; and that at least one of the classes in the cycle is annotated with `@Queryable` (or is referenced by a class which is).

To better illustrate how and why this issue occurs, let's use a simple example involving these classes:
```java
@Queryable(name = "A")
class A extends RealmObject {
    ...
    private B b;
    ...
}

class B extends RealmObject {
    ...
    private A a;
    ...
}
```
Say the user wants to build a query which returns `A` objects. What happens is that once they choose the kind of model they wish to build a query for (in this case, `A`), `RealmQueryView` makes a call to [`Ruqus.visibleFlatFieldsForClass(String)`][Ruqus Class], passing it the real name of the chosen model object class. That method, simply put, returns a list containing the following (in this case):

* Visible names of all fields on `A`, except those which were skipped (`@Ignore`/`@Hide`) and those which define a relationship (fields whose type is either some model object or a `RealmList` of model objects; in our example, the field `b` in class `A` falls into this category)
* For fields which define a relationship, such as field `b`, we traverse the relationship (`A-->B`) and:
    * The first rule is applied again, so we'd add all of `B`'s fields except for field `a`
    * Then the second rule is applied again, which causes us traverse the relationship `B-->A` to add `A`'s fields, thus creating a cycle

This should hopefully make it clear how cycles can cause issues. It also exposes the solution, which is to break cycles using the `@Hide` annotation.  
Ideally, one of the model classes involved in the cycle isn't annotated with `@Queryable`, in which case I'd recommend annotating the offending field in that class with `@Hide`:
```java
class B extends RealmObject {
    ...
    @Hide
    private A a;
    ...
}
```
However, you know your models' relationships better than I do, so you add the `@Hide` annotation where it will serve you best.  
My hope is that this will become a non-issue once the Android Realm library implements support for backlinks, since the current lack of support for them is what usually prompts us to build relationship cycles in the first place. In lieu of that, if someone knows of a clever way to defeat this issue I'd love to hear it; open an issue or a pull request!

[Realm Java]: https://github.com/realm/realm-java
[SampleApplication Class]: blob/master/sample/src/main/java/com/bkromhout/ruqus/sample/SampleApplication.java
[EditQueryActivity Layout]: blob/master/sample/src/main/res/layout/activity_edit_query.xml
[EditQueryActivity Class]: blob/master/sample/src/main/java/com/bkromhout/ruqus/sample/EditQueryActivity.java
[Person Class]: blob/master/sample/src/main/java/com/bkromhout/ruqus/sample/models/Person.java
[Dog Class]: blob/master/sample/src/main/java/com/bkromhout/ruqus/sample/models/Dog.java
[Realm Link Queries]: https://realm.io/docs/java/latest/#link-queries
[Realm Ignore]: https://realm.io/docs/java/latest/#ignoring-properties
[Ruqus Class]: blob/master/ruqus-core/src/main/java/com/bkromhout/ruqus/Ruqus.java
