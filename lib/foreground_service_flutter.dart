library foreground_service_flutter;

import 'dart:io';
import 'package:flutter/services.dart';

/// Configuration model for the location service
class LocationConfig {
  final String accessToken;
  final String userId;
  final String carType;
  final String status;
  final String activeTrip;
  final String carColor;
  final NatsConfig natsConfig;

  LocationConfig({
    required this.accessToken,
    required this.userId,
    required this.carType,
    required this.status,
    required this.activeTrip,
    required this.carColor,
    required this.natsConfig,
  });

  Map<String, dynamic> toJson() => {
        'token': accessToken,
        'userId': userId,
        'carType': carType,
        'onlineStatus': status,
        'activeTrip': activeTrip,
        'carColor': carColor,
        ...natsConfig.toJson(),
      };
}

class NatsConfig {
  final String natsUrl;
  final String username;
  final String password;
  final String subject;

  NatsConfig({
    required this.natsUrl,
    required this.username,
    required this.password,
    required this.subject,
  });

  Map<String, dynamic> toJson() => {
        'natsUrl': natsUrl,
        'username': username,
        'password': password,
        'subject': subject,
      };
}

/// A Flutter plugin for handling foreground location services.
class ForegroundLocationService {
  static const MethodChannel _channel =
      MethodChannel('com.reuben.nats/foreground_location');

  /// Singleton instance
  static final ForegroundLocationService _instance =
      ForegroundLocationService._internal();

  /// Factory constructor to return the singleton instance
  factory ForegroundLocationService() => _instance;

  /// Private constructor
  ForegroundLocationService._internal();

  /// Start or stop the foreground location service
  ///
  /// [isOnline] determines whether to start or stop the service
  /// [config] contains all the necessary configuration parameters
  Future<void> triggerForegroundLocation({
    required bool isOnline,
    LocationConfig? config,
  }) async {
    if (!Platform.isAndroid) {
      throw PlatformException(
        code: 'UNSUPPORTED_PLATFORM',
        message: 'This plugin only supports Android platforms.',
        details: 'Current platform: ${Platform.operatingSystem}',
      );
    }

    try {
      if (isOnline) {
        if (config == null) {
          throw ArgumentError(
              'LocationConfig is required when starting the service');
        }
        await _channel.invokeMethod('startService', config.toJson());
      } else {
        await _channel.invokeMethod('stopService');
      }
    } on PlatformException catch (e) {
      print('ForegroundLocationService error: ${e.message}');
      rethrow;
    }
  }
}
