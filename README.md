# sneaker_backend

Local PostgreSQL can use `DATABASE_URL_LOCAL` with Spring's datasource username and password environment variables. Render uses `DATABASE_URL`. Set `SNEAKERSHOP_LOCAL_DEMO=true` only for local development that needs demo password resets or legacy management routes.

## Stripe sandbox checkout

Set `STRIPE_SECRET_KEY` to the sandbox API key and `STRIPE_WEBHOOK_SECRET` to the signing secret for the webhook destination. Set `STRIPE_CHECKOUT_FRONTEND_URL` to the storefront origin when it differs from `http://localhost:3000`. Spring applies the checkout migration when it starts against the existing PostgreSQL schema.

For local testing, use the Stripe CLI and set `STRIPE_WEBHOOK_SECRET` to the signing secret it prints before starting Spring.

```sh
stripe listen --events checkout.session.completed,checkout.session.async_payment_succeeded,checkout.session.async_payment_failed,checkout.session.expired \
  --forward-to localhost:8080/api/v1/checkout/webhook
```

On Render, configure the same four events on a Stripe sandbox webhook destination at `https://<backend-host>/api/v1/checkout/webhook` and use that destination's signing secret. Checkout prices are in EUR and Stripe collects an Austrian shipping address.

## Order confirmation email

Set `RESEND_API_KEY` to a Resend sending key and `CHECKOUT_CONFIRMATION_TEST_RECIPIENT` to your Resend account email. Enter that same email address in Stripe Checkout. The default sender is `SneakerShop <onboarding@resend.dev>`. When using a verified sending domain, set `CHECKOUT_CONFIRMATION_FROM` to an address on that domain.

Spring queues one confirmation for a verified paid order if the Stripe Checkout email matches the configured test recipient. Mail delivery runs separately from payment processing and retries with a stable Resend idempotency key.
