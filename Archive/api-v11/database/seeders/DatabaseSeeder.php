<?php

namespace Database\Seeders;

use App\Models\Bus;
use App\Models\Driver;
use App\Models\Violation;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // Create 10 drivers
        $drivers = [];
        for ($i = 1; $i <= 10; $i++) {
            $drivers[] = Driver::create([
                'name' => 'Driver ' . $i,
            ]);
        }

        // Create 5 recent violations
        $types = ['Speeding', 'Off Route', 'Harsh Braking', 'Late Arrival'];
        foreach ($drivers as $index => $driver) {
            if ($index < 5) {
                Violation::create([
                    'driver_id' => $driver->id,
                    'route' => 'Route ' . rand(1, 20),
                    'type' => $types[array_rand($types)],
                    'created_at' => now()->subMinutes(rand(1, 60)),
                ]);
            }
        }

        // Create some buses with coordinates
        $statuses = ['Moving', 'Stopped', 'Delayed', 'Offline'];
        for ($i = 1; $i <= 20; $i++) {
            Bus::create([
                // Roughly around a city center (e.g. 6.9271, 79.8612 for Colombo just as an example)
                'lat' => 6.9271 + (rand(-100, 100) / 10000),
                'lng' => 79.8612 + (rand(-100, 100) / 10000),
                'status' => $statuses[array_rand($statuses)],
            ]);
        }
    }
}
