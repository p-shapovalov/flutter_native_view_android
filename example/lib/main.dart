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
        home: const HomePage(),
      ),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _currentIndex = 0;
  final List<ViewOption> _views = [
    ViewOption('red_view', 'Red View', Colors.red),
    ViewOption('green_view', 'Green View', Colors.green),
    ViewOption('blue_view', 'Blue View', Colors.blue),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text('Native View Example'),
        backgroundColor: Colors.white.withValues(alpha: 0.9),
        elevation: 2,
      ),
      body: Stack(
        children: [
          // Native view - lifecycle managed by NativeViewWidget
          Positioned.fill(
            child: NativeViewOverlayBody(
              enabled: true,
              child: ColoredNativeView(
                key: ValueKey(_views[_currentIndex].key),
                viewKey: _views[_currentIndex].key,
              ),
            ),
          ),

          // Flutter UI overlay
          Positioned(
            left: 16,
            right: 16,
            bottom: 32,
            child: _buildControlPanel(),
          ),

          Positioned(top: 120, right: 16, child: _buildInfoCard()),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _cycleView,
        tooltip: 'Next View',
        child: const Icon(Icons.swap_horiz),
      ),
    );
  }

  Widget _buildControlPanel() {
    return Card(
      elevation: 8,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Select Native View',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _views.asMap().entries.map((entry) {
                final isSelected = entry.key == _currentIndex;
                return ChoiceChip(
                  label: Text(entry.value.name),
                  selected: isSelected,
                  onSelected: (_) => setState(() => _currentIndex = entry.key),
                  avatar: CircleAvatar(
                    backgroundColor: entry.value.color,
                    radius: 10,
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 16),
            const Text(
              'Try scrolling in the native view area above!',
              style: TextStyle(fontSize: 12, color: Colors.grey),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoCard() {
    final current = _views[_currentIndex];
    return Card(
      elevation: 4,
      child: Container(
        padding: const EdgeInsets.all(12),
        width: 140,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: current.color,
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            const SizedBox(height: 8),
            Text(current.name, style: const TextStyle(fontWeight: FontWeight.w500)),
            const SizedBox(height: 4),
            const Text(
              'Active',
              style: TextStyle(fontSize: 12, color: Colors.green),
            ),
          ],
        ),
      ),
    );
  }

  void _cycleView() {
    setState(() => _currentIndex = (_currentIndex + 1) % _views.length);
  }
}

class ViewOption {
  final String key;
  final String name;
  final Color color;

  ViewOption(this.key, this.name, this.color);
}

/// NativeViewWidget subclass - lifecycle managed automatically
class ColoredNativeView extends NativeViewWidget {
  const ColoredNativeView({super.key, required this.viewKey});

  @override
  final String viewKey;
}
