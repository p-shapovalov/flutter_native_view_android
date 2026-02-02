import 'package:flutter/material.dart';
import 'package:flutter_native_view_android/flutter_native_view_android.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return NativeViewOverlayApp(
      enabled: true,
      child: MaterialApp(
        title: 'Native View Example',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
          useMaterial3: true,
        ),
        home: const MainPage(),
      ),
    );
  }
}

/// Main page with the red native view
class MainPage extends StatelessWidget {
  const MainPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text('Main Page'),
        backgroundColor: Colors.white.withValues(alpha: 0.9),
        elevation: 2,
      ),
      body: Stack(
        children: [
          // Red native view
          Positioned.fill(
            child: NativeViewOverlayBody(
              enabled: true,
              child: const ColoredNativeView(
                key: ValueKey('red_view'),
                viewKey: 'red_view',
              ),
            ),
          ),

          // Flutter UI overlay - navigation card
          Positioned(
            left: 16,
            right: 16,
            bottom: 32,
            child: Card(
              elevation: 8,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 24,
                          height: 24,
                          decoration: BoxDecoration(
                            color: Colors.red,
                            borderRadius: BorderRadius.circular(4),
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Red Native View',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'Scroll the native view above. Tap the button to see another native view on a separate page.',
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                    const SizedBox(height: 16),
                    FilledButton.icon(
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => const SecondPage(),
                          ),
                        );
                      },
                      icon: const Icon(Icons.arrow_forward),
                      label: const Text('Open Green View Page'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Second page with the green native view
class SecondPage extends StatelessWidget {
  const SecondPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text('Second Page'),
        backgroundColor: Colors.white.withValues(alpha: 0.9),
        elevation: 2,
      ),
      body: Stack(
        children: [
          // Green native view
          Positioned.fill(
            child: NativeViewOverlayBody(
              enabled: true,
              child: const ColoredNativeView(
                key: ValueKey('green_view'),
                viewKey: 'green_view',
              ),
            ),
          ),

          // Flutter UI overlay - info card
          Positioned(
            left: 16,
            right: 16,
            bottom: 32,
            child: Card(
              elevation: 8,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 24,
                          height: 24,
                          decoration: BoxDecoration(
                            color: Colors.green,
                            borderRadius: BorderRadius.circular(4),
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Green Native View',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      'This is a different native view on a separate page. Each page manages its own native view lifecycle.',
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// NativeViewWidget subclass - lifecycle managed automatically
class ColoredNativeView extends NativeViewWidget {
  const ColoredNativeView({super.key, required this.viewKey});

  @override
  final String viewKey;
}
