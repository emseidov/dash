# DASH | PLEASE WATCH THE DEMO VIDEO

Programmable UI and dashboard builder, inspired by Lisp macros, code that writes code, but visual.

<https://github.com/user-attachments/assets/95e5ed55-55e0-4311-a440-adaba63c6b52>

endpoints used in the demo:

- `https://api.frankfurter.dev/v1/currencies` for dropdown-widget
- `https://api.frankfurter.dev/v1/{arg1}..{arg2}?base={arg3}` for table-widget

## Notes

The application is built using [day8 re-frame template](https://github.com/day8/re-frame-template) to avoid spending time on configuration. My goal was to demonstrate solid understanding of Clojure and re-frame, and show ability to design and implement complex applications. For this reason, I didn't spend any time on polishing the UI.

## To run application

1. Install dependencies

```
npm i
```

2. Start dev server

```
npm run watch
```

3. Navigate to `http://localhost:8280/`
