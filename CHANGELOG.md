# Ruqus Changelog

## 1.2.3
* Corrected string in dialog for removing conditions; now says "Condition" or "Operator" depending on type
* No more unformatted dates in `RealmQueryView`'s main mode
* Improved human-readable query strings, especially in the areas of handling string-related conditions and how the `Not` operator affects each condition's string
    * To support this change, all `RUQTransformer`s now must implement a new method, `makeReadableString(Condition current, Condition previous, Condition next)`. See the JavaDoc for more information.
    * Since the pairing of the `Not` and `Equal` operators now displays as *"is not"* in query strings, the name of the `NotEqualTo` operator has been changed from *"is not"* to *"is unequal"* to help distinguish it... sorry for any confusion!
* Now the query type and sort type cards (the top and bottom cards) are visually distinguished with an alternate color
* Card background colors can now be overridden. See the end of the README for details

## 1.2.2
* Fixed a bug with a method in `Ruqus` which, looking at it now, I'm at a loss as to how it ever worked

## 1.2.1
* Fixed bug where the `Not` transformer was causing crashes due to a missed "`break;`" in the `Condition` class

## 1.2.0
* Tested with Realm 1.0.0 and confirmed working
* Added a transformer, `Not`, to wrap `RealmQuery.not()`. This is particularly important for link queries since `.notEqualTo()` and `.not().equalTo()` work differently for `RealmList` fields
* Added method `RealmQueryView.leaveBuilderMode()` to help improve integration with host. See the end of [Adding a `RealmQueryView`](https://github.com/bkromhout/ruqus#rqv) for more information
* Views now have touch feedback

## 1.1.0
* Support for Realm 0.89.0; Ruqus will now handle any `RealmModel`-implementing class (which still includes `RealmObject` subclasses, of course)

## 1.0.6
* Followup fix for 1.0.5.

## 1.0.5
* Fixed an issue with RealmUserQuery's parsing of internal RealmUserQuery strings.

## 1.0.4
* Added support for [AboutLibraries](https://github.com/mikepenz/AboutLibraries)

## 1.0.3
* Fixed an off by one error.

## 1.0.2
* Made ruqus-compiler ignore static fields on classes when processing them.

## 1.0.1
* Added ability for `RealmQueryView` to notify a listener about mode changes

## 1.0.0
* Initial release
