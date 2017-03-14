# App Smoke Tests

These tests will test that the app has been successfully deployed.
They do not rely on any specific data, and therefore should work against any
deployed instance of the application.

## Usage

`bin/test <app_url>` where `<app_url>` is the base url of the
attendee service application you'd like to test. For example:

```bash
bin/test http://localhost:8181
```
