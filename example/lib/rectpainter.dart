import 'package:flutter/material.dart';

class RectPainter extends CustomPainter {
  Map? event;
  RectPainter(this.event);
  @override
  void paint(Canvas canvas, Size size) {
    if (event == null)
      return;
    else {
      // print('--------------------------------canvas size $size');
      double scale = size.width / 480.0;
      double left = event!["left"] * scale;
      double top = event!["top"] * scale;
      double right = event!["right"] * scale;
      double bottom = event!["bottom"] * scale;
      Rect rect = Rect.fromLTRB(left, top, right, bottom);

      final paint = Paint()
        ..color = Colors.yellow
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.0;
      canvas.drawRect(rect, paint);

      TextSpan span = TextSpan(
        text: event!["result"],
        style: TextStyle(color: Colors.white, fontSize: 20, backgroundColor: Colors.black26,),
      );
      TextPainter painter = TextPainter(
        text: span,
        maxLines: 2,
        textDirection: TextDirection.ltr,
      );
      painter.layout(maxWidth: size.width);
      painter.paint(canvas, Offset((size.width - painter.width) * 0.5, 10));
    }
  }

  @override
  bool shouldRepaint(RectPainter oldDelegate) => oldDelegate.event != event;
}
