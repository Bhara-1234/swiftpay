import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    payment_test: {
      executor: 'constant-arrival-rate',
      rate: 250,
      timeUnit: '1s',
      preAllocatedVUs: 300,
      maxVUs: 5000,
      duration: '24h'
    }
  }
};

export default function () {

  // Random sender between 1 and 10
  const senderId = Math.floor(Math.random() * 10) + 1;

  // Random receiver between 1 and 10, but different from sender
  let receiverId;
  do {
    receiverId = Math.floor(Math.random() * 10) + 1;
  } while (receiverId === senderId);

  const payload = JSON.stringify({
    transactionId: `SP-${Date.now()}-${Math.random()}`,
    senderId: senderId,
    receiverId: receiverId,
    amount: 100,
    currency: 'INR'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json'
    }
  };

  let res = http.post(
    'http://100.31.251.13:8080/v1/payments',
    payload,
    params
  );

  check(res, {
    'status is 200 or 202': (r) =>
      r.status === 200 || r.status === 202
  });
}
