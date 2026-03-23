# TransitShield Android App

## Backend base URL configuration

TransitShield Android supports both emulator and real-device backend access.

### Emulator (default)
- Use `10.0.2.2` in Login screen **Settings**.
- App calls backend at `http://10.0.2.2:8080/api/`.

### Real phone on same Wi-Fi as laptop backend
1. Start backend on laptop (`0.0.0.0:8080` or normal local bind).
2. Find laptop LAN IP (example `192.168.1.120`).
3. In Android Login screen, tap **Settings** icon.
4. Enter laptop IP only (example `192.168.1.120`) and Save.
5. App will use `http://<ip>:8080/api/` automatically.

## Driver ticket flow
- Driver scans passenger ticket QR from **Driver Dashboard -> Scan Ticket**.
- Passenger shows QR from **Active Trip -> Show Ticket QR**.
- Validation happens via backend `/api/driver/tickets/validate`.

