<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Bus;
use App\Models\Violation;

class DashboardController extends Controller
{
    public function index()
    {
        $activeBuses = Bus::whereIn('status', ['Moving', 'Stopped', 'Delayed'])->count();
        
        $stats = [
            'active_buses' => $activeBuses > 0 ? $activeBuses : 142, // fallback to exactly 142 if DB is empty as requested
            'total_complaints' => 12,
            'lost_items' => 8,
            'demerit_warnings' => 3
        ];
        
        $recentViolations = Violation::with('driver')
            ->orderBy('created_at', 'desc')
            ->take(5)
            ->get();
            
        $mapMarkers = Bus::select('id', 'lat', 'lng', 'status')->get();
        
        return response()->json([
            'stats' => $stats,
            'recent_violations' => $recentViolations,
            'map_markers' => $mapMarkers
        ]);
    }
}
