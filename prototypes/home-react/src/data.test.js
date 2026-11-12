import { test } from 'node:test';
import assert from 'node:assert/strict';
import { occurrences, nextPayment, monthly, initialState, demoRepository } from './data.js';
test('fresh demo starts empty',()=>{const state=initialState();assert.deepEqual(state.subscriptions,[]);assert.equal(state.budget,0)});
test('month end clamps and restores original billing day',()=>{const s={startDate:'2024-01-31',cycle:'month',price:1000};assert.equal(occurrences(s,2024,1)[0].getDate(),29);assert.equal(occurrences(s,2024,2)[0].getDate(),31);assert.equal(occurrences(s,2023,11).length,0)});
test('annual plans bill once and normalize for budget',()=>{const s={startDate:'2024-02-29',cycle:'year',price:120000};assert.equal(occurrences(s,2025,1)[0].getDate(),28);assert.equal(occurrences(s,2025,2).length,0);assert.equal(monthly(s),10000)});
test('ended plans stop recurring',()=>{const s={startDate:'2024-01-01',endedAt:'2024-03-01',cycle:'month'};assert.equal(occurrences(s,2024,2).length,1);assert.equal(occurrences(s,2024,3).length,0)});
test('next payment handles distant starts',()=>{const s={startDate:'2035-05-31',cycle:'month'};const date=nextPayment(s,new Date(2026,0,1));assert.equal(date.getFullYear(),2035);assert.equal(date.getDate(),31)});
test('broken storage falls back to clean state',()=>{globalThis.localStorage={getItem:()=>'{broken'};assert.deepEqual(demoRepository.load(),initialState());delete globalThis.localStorage});
