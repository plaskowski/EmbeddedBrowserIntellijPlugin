# EmbeddedBrowserIntellijPlugin

![Build](https://github.com/plaskowski/EmbeddedBrowserIntellijPlugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.plaskowski.embeddedbrowserintellijplugin.svg)](https://plugins.jetbrains.com/plugin/com.github.plaskowski.embeddedbrowserintellijplugin)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.plaskowski.embeddedbrowserintellijplugin.svg)](https://plugins.jetbrains.com/plugin/com.github.plaskowski.embeddedbrowserintellijplugin)

<!-- Plugin description -->
This is a plugin for Intellij providing a browser embedded into tool window
so that you can watch cat videos while your Spring context is starting.
Inspired by https://github.com/Jonatha1983/GIdeaBrowser.
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "EmbeddedBrowser"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/plaskowski/EmbeddedBrowserIntellijPlugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration

As for now the plugin options can set by editing `.idea/workspace.xml` (remember to close the IDE before editing it).

You can add following optional properties to PropertiesComponent in `.idea/workspace.xml`:
- `com.github.plaskowski.embeddedbrowserintellijplugin.initial_url` - the initial URL to load
- `com.github.plaskowski.embeddedbrowserintellijplugin.cache_path` - a folder where browser should store its state 
  - configure this if you want the browser state (sessions, cookies) to persist between IDE restarts
  - :warning: this may not be secure, use it at your own risk
- `com.github.plaskowski.embeddedbrowserintellijplugin.user_agent` - custom User-Agent header
  - set mobile-like User-Agent to browse sites in their mobile versions that better fit limited tool window space

> :warning: *WARNING* `cache_path` and `user_agent` modify global CEF settings so other CEF instances in IDE will be affected

Example:
```
  <component name="PropertiesComponent">
    ...
    <property name="com.github.plaskowski.embeddedbrowserintellijplugin.initial_url" value="https://youtube.com" />
    <property name="com.github.plaskowski.embeddedbrowserintellijplugin.cache_path" value="$USER_HOME$/.cef_cache" />
    <property name="com.github.plaskowski.embeddedbrowserintellijplugin.user_agent" 
      value="Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1" />
    ...
  </component>
```

## Roadmap

- Settings UI
- ...?


### Resources about plugin development

- https://plugins.jetbrains.com/docs/intellij/getting-started.html
- https://plugins.jetbrains.com/docs/intellij/welcome.html
- https://github.com/JetBrains/intellij-sdk-code-samples
- https://github.com/JetBrains/intellij-platform-plugin-template
- https://blog.jetbrains.com/platform/
- https://plugins.jetbrains.com/intellij-platform-explorer/

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
