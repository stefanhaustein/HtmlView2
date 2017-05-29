# HtmlView2

Cleaner, simplified and "Androidified" version of HtmlView .

- Custom text styling using Views in HtmlView1 replaced with Android spans.
- Layouts are now orthogonal to the view hierarchy (opposed to subclassing in HtmlView).
- Native views don't require wrapping.

## Restrictions

- No support for CSS floats
- CSS can't override whether an element is a text or block element.

## Usage

For a simple example, please refer to the [demo](
https://github.com/stefanhaustein/HtmlView2/blob/master/demo/src/main/java/org/kobjects/htmlview2/demo/MainActivity.java)

## Gradle

Jitpack for the win!

Step 1: Add jitpack to your root build.gradle at the end of repositories:

    allprojects {
		    repositories {
			  ...
			  maven { url 'https://jitpack.io' }
		    }
	    }

Step 2: Add the HtmlView2 dependency

	dependencies {
		compile 'com.github.stefanhaustein:htmlview2:v2.0.2'
	}

